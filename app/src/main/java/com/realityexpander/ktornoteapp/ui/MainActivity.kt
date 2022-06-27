package com.realityexpander.ktornoteapp.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.realityexpander.ktornoteapp.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

val X509Certificate.fingerprint: String
    get() {
        val sha1 = MessageDigest.getInstance("SHA1")
        val digest = sha1.digest(encoded)
        val hexString = StringBuilder()
        for (byte in digest) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

object X509CertImpl {
    val SIGNATURE = "3cIon8YH9KpN25Zo32LiLzLmZm0=" // Debug mode
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printSigs()

        if (printAppSignature(applicationContext) == VALID) {
            println("App signature is valid")
        } else {
            println("App signature is invalid")
        }
    }

    private val VALID = 0
    private val INVALID = 1

    fun printAppSignature(context: Context): Int {
        try {
            val packageInfo: PackageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in packageInfo.signatures) {
                val signatureBytes = signature.toByteArray()
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val currentSignature: String = Base64.encodeToString(md.digest(), Base64.DEFAULT)

                println("REMOVE_ME: Include this string as a value for SIGNATURE:$currentSignature")

                //compare signatures
                if (X509CertImpl.SIGNATURE == currentSignature) {
                    return VALID
                }
            }
        } catch (e: Exception) {
            //assumes an issue in checking signature., but we let the caller decide on what to do.
        }
        return INVALID
    }

    fun printSigs() {
        val sigs: Array<Signature> = applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            PackageManager.GET_SIGNATURES
        ).signatures
        for (sig in sigs) {
            println("Signature hashcode: " + sig.hashCode())
        }

        val sig = applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName, PackageManager.GET_SIGNATURES
        ).signatures[0]
        //    val releaseSig = context!!.packageManager.getPackageArchiveInfo(
        //        "/mnt/sdcard/myReleaseApk.apk",
        //        PackageManager.GET_SIGNATURES
        //        )!!.signatures[0]
        println("Signature hashcode: " + sig.hashCode())
        println("Signature hashcode: hex=${String.format("%010x", sig.hashCode())}")

        val packageManager = applicationContext.packageManager
        val packageList = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)
        var sb = StringBuilder()

        for (p in packageList) {
            val strName = p.applicationInfo.loadLabel(packageManager).toString()
            val strVendor = p.packageName

//            if (strVendor == "com.realityexpander.ktornoteapp") {
            if(strVendor == applicationContext.packageName) {
                sb.append("--- signature ---\n")
                sb.append("$strName / $strVendor\n")

                val arrSignatures = p.signatures
                for (sig in arrSignatures) {
                    /*
                    * Get the X.509 certificate.
                    */
                    val rawCert = sig.toByteArray()
                    val certStream: InputStream = ByteArrayInputStream(rawCert)
                    try {
                        val certFactory: CertificateFactory = CertificateFactory.getInstance("X509")
                        val x509Cert: X509Certificate =
                            certFactory.generateCertificate(certStream) as X509Certificate
                        sb.append(
                            "Certificate subject: " + x509Cert.getSubjectDN().toString() + "\n"
                        )
                        sb.append("Certificate issuer: " + x509Cert.getIssuerDN().toString() + "\n")
                        sb.append(
                            "Certificate serial number: " + x509Cert.getSerialNumber()
                                .toString() + "\n"
                        )
                        sb.append("\n")
                    } catch (e: CertificateException) {
                        // e.printStackTrace();
                    }
                }
            }
        }
        println(sb)
    }

}