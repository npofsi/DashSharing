package pro.npofsi.dashsharing.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pro.npofsi.dashsharing.utils.FileX


class MainViewModel : ViewModel() {
    val is_sharing: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply { value = false }
    }
    val file_status: MutableLiveData<FileX.FileStatus> by lazy {
        MutableLiveData<FileX.FileStatus>().apply { value = FileX.FileStatus.UNSELECTED }
    }
    val file_name: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply { value = "..." }
    }


    fun update_file_status(status: FileX.FileStatus) {
        file_status.value = status
    }

    fun update_file_name(filename: String) {
        file_name.value = filename
    }

    fun update_is_sharing() {
        if (true == is_sharing.value) {
            is_sharing.value = false
        } else {
            if (file_status.value == FileX.FileStatus.LOADED) is_sharing.value = true
        }
    }

}