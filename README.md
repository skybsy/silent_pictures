# silent_pictures
## Android 摄像头静默拍照
### 使用
点击Activity中的按钮，退回到主界面，开始静默拍照，拍照成功后会弹出toast显示拍照成功。

此时只会拍摄一张照片，点击任意应用后退回到桌面会进行下一次拍照。

android 6.0以上需要手动打开window窗口
### 照片位置
/sdcard/silent/
需要先新建"silent"文件夹
### 方式
WindowManager实现，没用SurfaceView
### target version
android 4.4
