package com.example.kotlinplayground

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import DBConstant
import com.test.ClassModifyA
import com.test.ClassModifyB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lab.galaxy.yahfa.HookMain

private val TAG = "DEBUG"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test()
        testModification()
        testHook()
    }

    private fun testModification() {
        ClassModifyB().main()
    }

    private fun test(){
        GlobalScope.launch {
            val a = ClassA()
            Log.d(TAG, "Test Reflection")
            val methodA = a.javaClass.getDeclaredMethod("cut")

            val  b = StaticClassB()
            val methodB = b.javaClass.getDeclaredMethod("delete")
            methodB.isAccessible = true

            methodA.invoke(a)
            methodB.invoke(a)
        }
    }


    private fun testHook(){
        GlobalScope.launch {
            val a = StaticClassA()
            Log.d(TAG, "Test Hook")
            val methodA = a.javaClass.getDeclaredMethod("cut")
            methodA.isAccessible = true


            val  b = StaticClassB()
            val methodB = b.javaClass.getDeclaredMethod("delete")
            methodB.isAccessible = true


            HookMain.hook(methodA, methodB)

            methodA.invoke(a)
        }
    }

}


class StaticClassB {
    companion object {
        @JvmStatic
        private fun delete() {
            Log.d(TAG, "delete called ")
        }
    }
}

class StaticClassA{
    companion object {
        private var fieldA: Int = 0

        private fun copy() {
            Log.d(TAG, "copy called ")
        }

        @JvmStatic
        private fun cut() {
            Log.d(TAG, "cut called ")
        }
    }
}

class ClassA {
    private fun copy() {
        Log.d(TAG, "copy called ")
    }

    public fun cut() {
        Log.d(TAG, "cut called ")
    }
}



