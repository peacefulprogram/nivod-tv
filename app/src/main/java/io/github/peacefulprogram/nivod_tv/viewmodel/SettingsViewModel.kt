package io.github.peacefulprogram.nivod_tv.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_tv.NivodApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Proxy

class SettingsViewModel(
    val nivodApi: NivodApi
) : ViewModel() {

    private val _sp = NivodApp.settingSharedPreferences

    private val _networkSettings =
        MutableStateFlow(NetworkProxySettings.loadFromSharedPreference(_sp))

    val networkProxySettings: StateFlow<NetworkProxySettings>
        get() = _networkSettings

    fun applySetting(newSettings: NetworkProxySettings) {
        val currentSettings = _networkSettings.value
        if (currentSettings == newSettings) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            _networkSettings.emit(newSettings)
            val proxyConfig = if (newSettings.proxyEnabled && newSettings.proxyHost.isNotEmpty()) {
                Proxy(Proxy.Type.HTTP, InetSocketAddress(newSettings.proxyHost, newSettings.proxyPort))
            } else {
                null
            }
            nivodApi.recreateKtorClientWithProxy(proxyConfig)
        }
        newSettings.flushToSharedPreference(_sp)
    }

}

data class NetworkProxySettings(
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int
) {

    fun flushToSharedPreference(sp: SharedPreferences) {
        sp.edit().apply {
            putBoolean(proxyEnabledKey, proxyEnabled)
            putString(proxyHostKey, proxyHost)
            putInt(proxyPortKey, proxyPort)
        }.apply()
    }

    companion object {

        private const val proxyEnabledKey = "proxy.enable"
        private const val proxyHostKey = "proxy.host"
        private const val proxyPortKey = "proxy.port"

        fun loadFromSharedPreference(sp: SharedPreferences): NetworkProxySettings {
            return NetworkProxySettings(
                proxyEnabled = sp.getBoolean(proxyEnabledKey, false),
                proxyHost = sp.getString(proxyHostKey, "")!!,
                proxyPort = sp.getInt(proxyPortKey, 7890)
            )
        }
    }
}