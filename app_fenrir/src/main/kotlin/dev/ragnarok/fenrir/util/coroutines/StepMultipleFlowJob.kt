package dev.ragnarok.fenrir.util.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StepMultipleFlowJob {
    private val jobList = HashSet<PendingFlowJob<Any>>(1)

    @Suppress("unchecked_cast")
    fun <T> add(
        flow: Flow<T>,
        onSuccess: (result: T) -> Unit,
        onError: (exception: Throwable) -> Unit
    ): Boolean {
        synchronized(this) {
            return jobList.add(PendingFlowJob(flow, onSuccess, onError) as PendingFlowJob<Any>)
        }
    }

    fun fromIOToMain() {
        synchronized(this) {
            for (s in jobList) {
                CoroutineScope(Dispatchers.IO).launch {
                    s.flow.catch {
                        if (this@launch.isActive) {
                            this@launch.launch(Dispatchers.Main) {
                                s.onError(it)
                            }
                        }
                    }.collect {
                        if (this@launch.isActive) {
                            this@launch.launch(Dispatchers.Main) {
                                s.onSuccess.invoke(it)
                            }
                        }
                    }
                }
            }
            jobList.clear()
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
        val onSuccess: (result: T) -> Unit,
        val onError: (exception: Throwable) -> Unit
    )
}
