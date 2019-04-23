package com.example.windowdemo;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MyWindow extends LinearLayout implements SurfaceTextureListener {

	private TextureView textureView;

	/**
	 * 相机类
	 */
	private Camera myCamera;
	private Context context;

	private WindowManager mWindowManager;

	//启动线程拍照
	private static HandlerThread myhandlerthread = new HandlerThread("take picture");
	private Handler myWorker = new Handler(myhandlerthread.getLooper());

	static {
		myhandlerthread.start();
	}

	public MyWindow(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.window, this);
		this.context = context;
		
		initView();
	}

	private void initView() {

		textureView = (TextureView) findViewById(R.id.textureView);
		textureView.setSurfaceTextureListener(this);
		mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
	}

	@Override
	public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
		//开启线程进行拍照
		myWorker.post(new Runnable() {
			@Override
			public void run() {
				Log.d("log","thread is running");
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				int numberOfCameras = Camera.getNumberOfCameras();
				for(int i=0 ;i<numberOfCameras ;i++){
					Camera.getCameraInfo(i,cameraInfo);
					if(cameraInfo.facing== Camera.CameraInfo.CAMERA_FACING_FRONT){
						releaseCameraAndPreview();
						// 创建Camera实例
						myCamera = Camera.open(i);
						try {
							setCameraParameters();
							// 设置预览在textureView上
							myCamera.setPreviewTexture(surface);
							myCamera.setDisplayOrientation(SetDegree(MyWindow.this));

							// 开始预览
							myCamera.startPreview();

							//开始拍照
							myCamera.autoFocus(new Camera.AutoFocusCallback() {
								@Override
								public void onAutoFocus(boolean success, Camera camera) {
									myCamera.takePicture(null, null, new Camera.PictureCallback() {
										@Override
										public void onPictureTaken(byte[] data, Camera camera) {
											Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length);
											int degree = Utils.readPictureDegree(getFilePath());
											final Bitmap bitmap = Utils.rotateImageView(degree, source);
											String filePath = getFilePath();
											Utils.saveBitmap(bitmap, new File(filePath));
											Toast.makeText(context,"take a phone:"+filePath, Toast.LENGTH_SHORT).show();
										}
									});
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	private void setCameraParameters() {
		Camera.Parameters parameters = myCamera.getParameters();//获取各项参数
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setJpegQuality(100);
		int mPreiviewHeight = parameters.getPreviewSize().height;
		int mPreiviewWidth = parameters.getPreviewSize().width;
		parameters.setPreviewSize(mPreiviewWidth,mPreiviewHeight);
		//parameters.setPreviewFrameRate(5);
		myCamera.setParameters(parameters);
	}

	private void releaseCameraAndPreview() {
		if(myCamera!=null){
			myCamera.stopPreview();
			myCamera.release();
			myCamera=null;
		}
	}

	private int SetDegree(MyWindow myWindow) { 
		// 获得手机的方向
		int rotation = mWindowManager.getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		myCamera.stopPreview(); //停止预览
		myCamera.release();     // 释放相机资源
		myCamera = null;

		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

	private String getFilePath() {
		/*boolean canCreateOutside = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.isExternalStorageRemovable();
		if (canCreateOutside) {
			File filesExternalDir = context.getExternalFilesDir(null);
			if (filesExternalDir != null) {
				Log.d("tag","filepath:"+filesExternalDir.getPath() + "/" + System.currentTimeMillis() + ".jpg");
				return filesExternalDir.getPath() + "/" + System.currentTimeMillis() + ".jpg";
			}
		}
		return context.getFilesDir().getPath() + "/" + System.currentTimeMillis() + ".jpg";*/
		//String s = context.getFilesDir().getPath() + "/" +System.currentTimeMillis() + ".jpg";
		String s = "sdcard/silent/"+System.currentTimeMillis()+".jpg";
		Log.d("log",s);
		return s;
	}

}
