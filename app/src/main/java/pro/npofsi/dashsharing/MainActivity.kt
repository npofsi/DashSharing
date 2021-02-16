package pro.npofsi.dashsharing

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import pro.npofsi.dashsharing.databinding.ActivityMainBinding
import pro.npofsi.dashsharing.utils.FileX
import pro.npofsi.dashsharing.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    val REQUEST_GET_CONTENT = 1
    var cacheFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        cacheFilePath = this.obbDir.toString() + "/cache"
        (application as MainApplication).cacheFilePath = cacheFilePath


        if ((application as MainApplication).filename == null) initUIAndActions()
        linkUIAndActions()


    }

    @SuppressLint("ShowToast")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GET_CONTENT -> {
                    val uri = data?.data
                    if (uri != null) {


                        val name = getContentName(uri)
                        val mfilex = FileX(cacheFilePath)
                        mfilex.backward().mkdirs()
                        if (mfilex.exists()) mfilex.delete()
                        mfilex.createNewFile()

                        val indexfilex = FileX(cacheFilePath).backward()
                        indexfilex.forwardACNF("index.html").editor.writeString(
                            """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                            </head>
                            <body>
                                 <h1><a href="./cache?name=""" +
                                    name + """" download="""" + name + """">Download """ + name + """</a></h1>
                            </body>
                            </html>
                        """.trimIndent()
                        )
                        viewModel.update_file_status(FileX.FileStatus.LOADING)
                        Thread {
                            try {
                                mfilex.copyFromInputStream(contentResolver.openInputStream(uri))
                                runOnUiThread {
                                    viewModel.update_file_status(FileX.FileStatus.LOADED)
                                    (application as MainApplication).filename = getContentName(uri)
                                    (application as MainApplication).cacheFilePath = cacheFilePath
                                    viewModel.update_file_name(getContentName(uri))
                                }

                            } catch (e: Exception) {
                                runOnUiThread(Runnable {
                                    initUIAndActions()
                                    stopHttpService()
                                    Toast.makeText(
                                        this,
                                        "Can not access to file.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                })
                            }
                        }.start()

                    } else {
                        Toast.makeText(this, "Can not access to file.", Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    fun initUIAndActions() {
        viewModel.apply {
            is_sharing.value = false
            file_name.value = "..."
            file_status.value = FileX.FileStatus.UNSELECTED
        }

        (application as MainApplication).apply {
            cacheFilePath = null
            filename = null
            isServiceRunning = false
        }

    }

    fun linkUIAndActions() {
        if ((application as MainApplication).filename != null) {
            viewModel.file_name.postValue((application as MainApplication).filename)
            viewModel.file_status.postValue(FileX.FileStatus.LOADED)
        }

        viewModel.is_sharing.postValue((application as MainApplication).isServiceRunning)
        viewModel.file_name.observe(this, { binding.fileNameTextview.text = it })
        viewModel.file_status.observe(this, {
            when (it) {
                FileX.FileStatus.UNSELECTED -> {
                    binding.progressBar.apply {
                        isIndeterminate = false
                        max = 1
                        progress = 0
                    }
                    binding.fileSelectButton.apply {
                        isEnabled = true
                        isClickable = true
                        setTextColor(getColor(R.color.white))
                    }
                    binding.sharingControlButton.apply {
                        isEnabled = false
                        isClickable = false
                        imageTintList = ColorStateList.valueOf(getColor(R.color.grey))
                    }
                }
                FileX.FileStatus.LOADING -> {
                    binding.progressBar.apply {
                        isIndeterminate = true
                    }
                    binding.fileSelectButton.apply {
                        isEnabled = false
                        isClickable = false
                        setTextColor(getColor(R.color.grey))

                    }
                    binding.sharingControlButton.apply {
                        isEnabled = false
                        isClickable = false
                        imageTintList = ColorStateList.valueOf(getColor(R.color.grey))
                    }
                }
                FileX.FileStatus.LOADED -> {
                    binding.progressBar.apply {
                        isIndeterminate = false
                        max = 1
                        progress = 1
                    }
                    binding.fileSelectButton.apply {
                        isEnabled = true
                        isClickable = true
                        setTextColor(getColor(R.color.white))

                    }
                    binding.sharingControlButton.apply {
                        isEnabled = true
                        isClickable = true
                        imageTintList = null
                    }
                }
            }
        })
        viewModel.is_sharing.observe(this, {
            if (it) {
                binding.sharingControlButton.setImageResource(R.drawable.baseline_pause_white_24)
                startHttpService()
            } else {
                binding.sharingControlButton.setImageResource(R.drawable.baseline_play_arrow_white_24)
                stopHttpService()
            }
        })
        binding.fileSelectButton.setOnClickListener { getContent(it) }
        binding.sharingControlButton.setOnClickListener { viewModel.update_is_sharing() }
    }

    fun startHttpService() {
        startService((application as MainApplication).httpServiceIntent)
    }

    fun stopHttpService() {
        stopService((application as MainApplication).httpServiceIntent)
    }


    fun getContent(v: View) {


        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, REQUEST_GET_CONTENT)
    }

    fun getContentName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = this.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        if (result == null) {
            result = "Can not access to file name."
        }
        return result
    }
}