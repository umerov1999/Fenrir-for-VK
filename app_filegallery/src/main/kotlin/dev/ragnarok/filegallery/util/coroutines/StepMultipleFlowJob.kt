package dev.ragnarok.filegallery.util.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StepMultipleFlowJob {
    private val jobList = HashSet<PendingFlowJob<Any>>(1)

    @Suppress("unchecked_cast")
    fun <T> add(
        flow: Flow<T>,
        onSuccess: suspend CoroutineScope.(result: T) -> Unit,
        onError: suspend CoroutineScope.(exception: Throwable) -> Unit
    ): Boolean {
        synchronized(this) {
            return jobList.add(PendingFlowJob(flow, onSuccess, onError) as PendingFlowJob<Any>)
        }
    }

    fun fromIOToMain() {
        val list = synchronized(this) {
            jobList.toList().also { jobList.clear() }
        }
        CoroutineScope(Dispatchers.IO).launch {
            list.forEach { i ->
                i.flow.catch {
                    withContext(Dispatchers.Main) {
                        i.onError.invoke(this, it)
                    }
                }.collect {
                    withContext(Dispatchers.Main) {
                        i.onSuccess.invoke(this, it)
                    }
                }
            }
        }
    }

    fun size(): Int {
        var ret = 0
        synchronized(this) {
            ret = jobList.size
        }
        return ret
    }

    private data class PendingFlowJob<T>(
        val flow: Flow<T>,
        val onSuccess: suspend CoroutineScope.(result: T) -> Unit,
        val onError: suspend CoroutineScope.(exception: Throwable) -> Unit
    )
}
