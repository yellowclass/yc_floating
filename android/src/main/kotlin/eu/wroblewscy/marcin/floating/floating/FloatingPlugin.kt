package eu.wroblewscy.marcin.floating.floating

import android.app.Activity
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*
import kotlin.concurrent.fixedRateTimer

/** FloatingPlugin */
class FloatingPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private lateinit var activity: Activity
  private  var timer: Timer?= null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "floating")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "enablePip") {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        result.success(activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build()))
      } else {
        result.success(activity.enterPictureInPictureMode())
      }
    } else if (call.method == "disablePip") {
      result.success(activity.moveTaskToBack(true))
    } else if (call.method == "pipAvailable") {
      result.success(
          activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
      )
    } else if (call.method == "inPipAlready") {
      result.success(
          activity.isInPictureInPictureMode
      )
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {
    cleanup()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    useBinding(binding)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    useBinding(binding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    cleanup()
  }

  fun useBinding(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  fun cleanup() {
    timer?.cancel()
  }
}
