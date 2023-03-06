package com.playground.unsafe

import sun.misc.Unsafe
import java.lang.reflect.Field

fun main() {
     val unsafe = getUnsafe()
     val instance = unsafe.allocateInstance(InitializationOrdering::class.java) as InitializationOrdering
     println(instance.a)
}

private fun getUnsafe(): Unsafe {
     val f: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
     f.isAccessible = true
     return f.get(null) as Unsafe
}

private class InitializationOrdering {
     val a: Long = 1
}

