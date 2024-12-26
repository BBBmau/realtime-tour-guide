import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.example.exploreai.shared.AssistantScreen

class CarAssistantSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return AssistantScreen(carContext)
    }
}