package pro.npofsi.dashsharing.utils;
import java.io.*;

import java.util.*;
import android.util.Log;

public class FileX extends File
{
    public static final String TAG = FileX.class.getSimpleName();
	public FileX(String path){
		super(path);
	}
	
	public static FileX toFileX(File file){
		return new FileX(file.getAbsolutePath());
	}
	
	public FileX[] listFileXFiles(){
		File[] files=this.listFiles()==null?new File[]{}:this.listFiles()==null?new File[]{}:this.listFiles();
		FileX[] fileXs= new FileX[this.countFiles()];
		int i=0;
		for(File file: files != null ? files : new File[0]){
			if(file.isFile()){
				fileXs[i]=FileX.toFileX(file);
				i++;
			}
		}
		files = null;
		return fileXs;
	}
	
	public FileX[] listFileXDirectorys(){
		File[] files=this.listFiles()==null?new File[]{}:this.listFiles();
		FileX[] fileXs= new FileX[this.countDirectory()];
		int i=0;
		for(File file: files != null ? files : new File[0]){
			if(file.isDirectory()){
				fileXs[i]=FileX.toFileX(file);
				i++;
			}
		}
		files = null;
		return fileXs;
	}
	public FileX[] listFileXs(FilenameFilter fnf){
		File[] files=this.listFiles(fnf);
		FileX[] fileXs= new FileX[files.length];
		int i=0;
		for(File file:files){
			fileXs[i]=FileX.toFileX(file);
			i++;
		}
		files = null;
		return fileXs;
	}
	
	public FileX[] listFileXs(FileFilter ff){
		File[] files=this.listFiles(ff);
		FileX[] fileXs= new FileX[files.length];
		int i=0;
		for(File file:files){
			fileXs[i]=FileX.toFileX(file);
			i++;
		}
		files = null;
		return fileXs;
	}
	
	public FileX[] listFileXs(){
		File[] files=this.listFiles()==null?new File[]{}:this.listFiles();
		FileX[] fileXs= new FileX[files.length];
		int i=0;
		for(File file:files){
			fileXs[i]=FileX.toFileX(file);
			i++;
		}
		files = null;
		return fileXs;
	}
	
	public FileX forward(String childName){
		String now=this.getAbsolutePath();
		if(childName.startsWith(separator)){
			childName=childName.replaceFirst(separator,"");
		}
		if(childName.endsWith(pathSeparator)){
			childName=childName.substring(0,childName.length()-separator.length()-1);
		}
		return new FileX(now+separator+childName);
	}
	
	public FileX forwardACNF(String childName){
		FileX f=this.forward(childName);
		try
		{

			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
		}
		catch (IOException e)
		{
            Log.e(TAG,e.getMessage(),e);
        }
		return f;
	}
	
	public FileX forwardAMKD(String childName){
		FileX f=this.forward(childName);
		f.mkdirs();
		return f;
	}
	
	public FileX backward(){
		return FileX.toFileX(this.getParentFile());
	}
	
	public FileX[] mkdirss(String[] childNames){
		FileX[] list=new FileX[childNames.length];
		int i=0;
		for(String childName:childNames){
			list[i]=this.forwardAMKD(childName);
			i++;
		}
		return list;
	}
	
	public FileX[] createNewFiles(String[] childNames){
		FileX[] list=new FileX[childNames.length];
		int i=0;
		for(String childName:childNames){
			list[i]=this.forwardACNF(childName);
			i++;
		}
		return list;
	}

	public void deleteX(){		
		if(this.isFile()&&this.exists()){
			this.delete();
		}
		if(this.isDirectory()&&this.exists()){
			FileX[] filexs = this.listFileXs();
			for(FileX filex:filexs){
				filex.deleteX();
			}
			this.delete();
			filexs = null;	// lets gc do its works
		}
	}
	
	public void deleteXU(final Runnable runnable){
		final FileX that=this;
		new Thread(new Runnable(){

				@Override
				public void run()
				{
					that.deleteX();
					runnable.run();
					// TODO: Implement this method
				}
			}).start();
	}
	
	public void clean(){
		FileX[] filexs = this.listFileXs();
		for(FileX filex:filexs){
			filex.deleteX();
		}
		filexs = null;	// lets gc do its works
	}
	
	public int countFiles(){
		if(this.isDirectory()){
			int i=0;
			for(File f:this.listFiles())if(f.isFile())i++;
			return i;
		}
		return 0;
	}
	public int countDirectory(){
		if(this.isDirectory()){
			int i=0;
			for(File f:this.listFiles())if(f.isDirectory())i++;
			return i;
		}
		return 0;
	}
	public int count(){
		if(this.isDirectory())return this.listFiles().length;
		return 0;
	}
	
	public boolean check(){
		if(!this.exists())return false;
		if(this.isFile()){
			return this.length() > 0;
		}
		return true;
	}
	public boolean[] checkList(String[] childNames){
		boolean[] list= new boolean[childNames.length];
		int i=0;
		for(String childName: childNames){
			list[i]=this.forward(childName).check();
			i++;
		}
		return list;
	}
	public boolean checkWhole(String[] childNames){
		boolean[] list=checkList(childNames);
		int i=0;
		for(boolean result: list){
			if(!result)i++;
		}
		return i == 0;
	}
	
	public FileInputStream fileInputStream=null;
	public FileOutputStream fileOutputStream=null;
	public FileInputStream getFileInputStream() throws FileNotFoundException, IOException{
		return new FileInputStream(this);
	}
	
	public FileOutputStream getFileOutputStream() throws FileNotFoundException, IOException{
		return new FileOutputStream(this);
		
	}
	
	public byte[] read() throws IOException{
		byte[] buffer = new byte[(int)this.length()];
		FileInputStream ist=this.getFileInputStream();
		ist.read(buffer);
		ist.close();
		return buffer;
	}
	
	public void write(byte[] buffer) throws IOException{
		FileOutputStream ost=this.getFileOutputStream();
		ost.write(buffer);
		ost.close();
		
	}
	
	public void copyTo(FileX f) throws IOException{
		if(f.isFile()){
            f.write(this.read());
        }else if(f.isDirectory()){
            FileX[] lf=f.listFileXs();
            for(FileX sf:lf){
                if(sf.isFile()){
                    sf.copyTo(f.forwardACNF(sf.getName()));
                }else if(sf.isDirectory()){
                    sf.copyTo(f.forwardAMKD(sf.getName()));
                }
            }
        }
		
	}
	
	public void copyFromInputStream(InputStream ist) throws IOException{
		//InputStream(ist).;
		byte[] b=new byte[1024];
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		while((ist.read(b))!=-1){
			bos.write(b);
		}
		bos.writeTo(this.getFileOutputStream());
		
		
	}
	
	public String getExtension(){
		String[] strn=this.getName().split("\\.");
		return strn.length>=1?strn[strn.length-1]:"";
	}
	
	
	public static class TempX extends FileX{	
		public static Stack<TempX> tempstack=new Stack<TempX>();
        FileX mTempDir;
		public static void removeAll(){
			for(Iterator<TempX> i=tempstack.iterator();i.hasNext();i.next().remove()){}
		}
		public TempX(FileX tempDir){
            super(tempDir.getAbsolutePath());
            mTempDir=tempDir;
			tempstack.add(this);
		}
        
        public FileX tempDir(){
            return mTempDir.forwardAMKD("tempxd_"+System.currentTimeMillis()+"_"+(int)(Math.random()*1000));
        }
        
        public FileX tempFile(){
            return mTempDir.forwardACNF("tempxf_"+System.currentTimeMillis()+"_"+(int)(Math.random()*1000));
        }
        
		public void remove(){
			this.deleteX();
		}
	}
    
    public Editor getEditor(){
        return new Editor(this);
    }
    public static class Editor{
        FileX f;
        public Editor(FileX file){
            f=file;
        }
        public String readString() throws IOException{

            if(f.isFile()){
            	byte[] buffer=f.read();
            	if(buffer.length>=2) {
					return new String(buffer);
				}
            }

			return "";
        }
        public void writeString(String str) throws IOException{
            f.write(str.getBytes());
        }
        public String[] readCSV() throws IOException{
			String[] split = readString().replace(" ","").split(",");
			//Log.e(TAG,f.getParentFile().getName()+" len:"+split.length+" items:"+split[0]);
			if(split.length==1)if(Objects.equals(split[0], ""))return new String[0];
			return split;
        }
        public void writeCSV(String[] strs) throws IOException{
            StringBuilder str= new StringBuilder();
            for (String buf:strs) {
                if(!Objects.equals(buf, "")) str.append(",").append(buf);
            }
            writeString(str.substring(1));
        }
        public FileX getFileX(){return f;}
    }
    public enum FileStatus{
		UNSELECTED,LOADING,LOADED
	}
}
