package com.huawei.huaweiscankit



import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val  DEFINED_CODE_WITHDRAW = 222
        private const val  DEFINED_CODE_DEPOSIT = 223
        private const val REQUEST_CODE_SCAN_WITHDRAW = 0X02
        private const val REQUEST_CODE_SCAN_DEPOSIT = 0X03
        private const val DEPOSIT_MONEY = "DepositMoney"
        private const val WITHDRAW_MONEY = "WithdrawMoney"

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun depositMoneyClick(view: View) {
        val list = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, list, DEFINED_CODE_DEPOSIT)
    }
    fun withdrawMoneyClick(view: View) {
        val list = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, list, DEFINED_CODE_WITHDRAW)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) return
        else if (requestCode == DEFINED_CODE_DEPOSIT) {
            // Call the barcode scanning view in Default View mode.
            ScanUtil.startScan(this, REQUEST_CODE_SCAN_DEPOSIT, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create())
        }
        else if (requestCode == DEFINED_CODE_WITHDRAW) {
            // Call the barcode scanning view in Default View mode.
            ScanUtil.startScan(this, REQUEST_CODE_SCAN_WITHDRAW, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create())
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //receive result after your activity finished scanning
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) { return }
        // Obtain the return value of HmsScan from the value returned by the onActivityResult method by using ScanUtil.RESULT as the key value.
        else if (requestCode == REQUEST_CODE_SCAN_DEPOSIT) {
            when (val obj: Any = data.getParcelableExtra(ScanUtil.RESULT)) {
                is HmsScan -> {
                    val depositMoney = obj.getOriginalValue()
                    if (!TextUtils.isEmpty(depositMoney) && depositMoney == DEPOSIT_MONEY) {
                        depositMoneyEdt.text.isNotEmpty().apply {
                            //RETROFIT CALL - Change User Money

                        }
                        Toast.makeText(this, obj.getOriginalValue(), Toast.LENGTH_SHORT).show()
                    }
                    return
                }
            }
        }
        else if (requestCode == REQUEST_CODE_SCAN_WITHDRAW) {
            when (val obj: Any = data.getParcelableExtra(ScanUtil.RESULT)) {
                is HmsScan -> {
                    val withdrawMoney = obj.getOriginalValue()
                    if (!TextUtils.isEmpty(withdrawMoney) && withdrawMoney == WITHDRAW_MONEY) {
                        withdrawMoneyEdt.text.isNotEmpty().apply {
                            //RETROFIT CALL - Change User Money
                        }
                        //RETROFIT CALL + User Money
                        Toast.makeText(this, obj.getOriginalValue(), Toast.LENGTH_SHORT).show()
                    }
                    return
                }
            }
        }
    }
}