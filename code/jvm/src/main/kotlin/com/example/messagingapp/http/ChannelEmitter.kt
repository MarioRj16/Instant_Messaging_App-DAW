import com.example.messagingapp.http.model.output.MessageOutputModel
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ChannelEmitter {
    private val listeners = ConcurrentHashMap<Int, SseEmitter>()

    fun addListener(
        userId: Int,
        listener: SseEmitter,
    ) {
        listeners[userId] = listener
        logger.info("Added listener for user $userId. Total listeners: ${listeners.size}")

        listener.onError {
            listeners.remove(userId)
            logger.info("Listener for user $userId errored. Total listeners: ${listeners.size}")
        }
        listener.onCompletion {
            listeners.remove(userId)
            logger.info("Listener for user $userId completed. Total listeners: ${listeners.size}")
        }
        listener.onTimeout {
            listeners.remove(userId)
            logger.info("Listener for user $userId timed out. Total listeners: ${listeners.size}")
        }
    }

    fun removeListener(userId: Int) {
        listeners[userId]?.complete()
        listeners.remove(userId)
        logger.info("Removed listener for user $userId. Total listeners: ${listeners.size}")
    }

    fun broadcast(message: MessageOutputModel) {
        logger.info("Total listeners: ${listeners.size}")
        listeners.values.forEach { listener ->
            try {
                listener.send(message)
                logger.info("Sent message ${message.content}")
                logger.info("Total listeners: ${listeners.size}")
            } catch (ex: IOException) {
                listener.completeWithError(ex)
            }
        }
    }

    companion object {
        private val logger = getLogger(ChannelEmitter::class.java)
    }
}
