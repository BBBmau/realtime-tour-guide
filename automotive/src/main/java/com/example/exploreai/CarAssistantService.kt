package com.example.exploreai

import CarAssistantSession
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class CarAssistantService : CarAppService() {
    override fun onCreateSession(): Session {
        return CarAssistantSession()
    }

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }
}