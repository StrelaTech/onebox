package com.strela.onebox.subscription

import org.json.JSONArray
import org.json.JSONObject

/**
 * Сборка xray-конфига из [Server].
 *
 * Конфиг содержит только outbound'ы (+ DNS и routing); inbound подставляет
 * [com.strela.onebox.xray.XrayController] в зависимости от режима (tun для VPN,
 * socks-порт для локального теста).
 */
object ConfigBuilder {

    /** Собирает конфиг для профиля. */
    fun build(server: Server, blockAds: Boolean = true): String {
        // Сервер, разобранный из готового JSON-конфига, хранит его snapshot:
        // конфиг провайдера (его DNS, routing, inbounds) авторитетнее пересборки.
        server.json.trim().takeIf { it.isNotEmpty() }?.let { return it }

        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "warning"))

        root.put("outbounds", outbounds(server))

        root.put("dns", JSONObject().apply {
            put("servers", JSONArray().put("1.1.1.1").put("8.8.8.8"))
            put("queryStrategy", "UseIPv4")
        })

        // Прямые маршруты: локальная сеть и DNS-серверы ядра — мимо прокси.
        root.put("routing", routing(server, blockAds))

        return root.toString(2)
    }

    /** Основной outbound профиля + freedom как fallback (direct). */
    private fun outbounds(server: Server): JSONArray {
        val main = when (server.protocol) {
            Server.Protocol.VLESS -> vless(server)
            Server.Protocol.VMESS -> vmess(server)
            Server.Protocol.TROJAN -> trojan(server)
            Server.Protocol.SHADOWSOCKS -> shadowsocks(server)
            Server.Protocol.SOCKS -> socks(server)
            Server.Protocol.UNKNOWN -> JSONObject().put("protocol", "freedom")
        }
        main.put("tag", "proxy")

        val direct = JSONObject().apply {
            put("protocol", "freedom")
            put("tag", "direct")
        }
        val block = JSONObject().apply {
            put("protocol", "blackhole")
            put("tag", "block")
        }
        return JSONArray().put(main).put(direct).put(block)
    }

    private fun routing(server: Server, blockAds: Boolean): JSONObject {
        val rules = JSONArray()

        // Приватные сети — напрямую: явные CIDR вместо geoip:private,
        // чтобы конфиг работал даже без geoip.dat (правила импортированных
        // JSON-конфигов с geoip:/geosite: ядру обеспечивает GeoAssets).
        rules.put(JSONObject().apply {
            put("type", "field")
            put("ip", JSONArray()
                .put("127.0.0.0/8")
                .put("10.0.0.0/8")
                .put("172.16.0.0/12")
                .put("192.168.0.0/16")
                .put("169.254.0.0/16")
                .put("::1/128")
                .put("fc00::/7")
                .put("fe80::/10"))
            put("outboundTag", "direct")
        })

        // Мусорные домены — в blackhole (без geosite, только явные домены).
        if (blockAds) {
            rules.put(JSONObject().apply {
                put("type", "field")
                put("domain", JSONArray()
                    .put("domain:doubleclick.net")
                    .put("domain:googleadservices.com")
                    .put("domain:googlesyndication.com")
                    .put("domain:google-analytics.com")
                    .put("domain:adservice.google.com")
                    .put("domain:app-measurement.com"))
                put("outboundTag", "block")
            })
        }

        return JSONObject().apply {
            put("domainStrategy", "IPIfNonMatch")
            put("rules", rules)
        }
    }

    // ── протоколы ──────────────────────────────────────────────────────────

    private fun vless(p: Server): JSONObject = JSONObject().apply {
        put("protocol", "vless")
        put("settings", JSONObject().apply {
            put("vnext", JSONArray().put(JSONObject().apply {
                put("address", p.address)
                put("port", p.port)
                put("users", JSONArray().put(JSONObject().apply {
                    put("id", p.credential)
                    put("encryption", "none")
                    if (p.extra.isNotEmpty()) put("flow", p.extra)
                }))
            }))
        })
        put("streamSettings", stream(p))
    }

    private fun vmess(p: Server): JSONObject = JSONObject().apply {
        put("protocol", "vmess")
        put("settings", JSONObject().apply {
            put("vnext", JSONArray().put(JSONObject().apply {
                put("address", p.address)
                put("port", p.port)
                put("users", JSONArray().put(JSONObject().apply {
                    put("id", p.credential)
                    put("alterId", p.extra.toIntOrNull() ?: 0)
                    put("security", "auto")
                }))
            }))
        })
        put("streamSettings", stream(p))
    }

    private fun trojan(p: Server): JSONObject = JSONObject().apply {
        put("protocol", "trojan")
        put("settings", JSONObject().apply {
            put("servers", JSONArray().put(JSONObject().apply {
                put("address", p.address)
                put("port", p.port)
                put("password", p.credential)
            }))
        })
        put("streamSettings", stream(p))
    }

    private fun shadowsocks(p: Server): JSONObject = JSONObject().apply {
        put("protocol", "shadowsocks")
        put("settings", JSONObject().apply {
            put("servers", JSONArray().put(JSONObject().apply {
                put("address", p.address)
                put("port", p.port)
                put("method", p.extra)
                put("password", p.credential)
            }))
        })
    }

    private fun socks(p: Server): JSONObject = JSONObject().apply {
        put("protocol", "socks")
        put("settings", JSONObject().apply {
            put("servers", JSONArray().put(JSONObject().apply {
                put("address", p.address)
                put("port", p.port)
                if (p.credential.isNotEmpty()) {
                    put("users", JSONArray().put(JSONObject().apply {
                        put("user", p.credential)
                        put("pass", p.extra)
                    }))
                }
            }))
        })
    }

    /**
     * streamSettings: transport + TLS/Reality.
     * Используется всеми протоколами, кроме ss (там шифрование своё).
     */
    private fun stream(p: Server): JSONObject = JSONObject().apply {
        val net = p.network.ifEmpty { "tcp" }
        put("network", net)
        put("security", p.security.ifEmpty { "none" })

        when (net) {
            "ws" -> put("wsSettings", JSONObject().apply {
                put("path", p.path.ifEmpty { "/" })
                // современное поле host вместо deprecated headers.Host
                if (p.host.isNotEmpty()) put("host", p.host)
            })

            "grpc" -> put("grpcSettings", JSONObject().apply {
                put("serviceName", p.serviceName)
            })

            "httpupgrade" -> put("httpupgradeSettings", JSONObject().apply {
                put("path", p.path.ifEmpty { "/" })
                if (p.host.isNotEmpty()) put("host", p.host)
            })

            "splithttp", "xhttp" -> put("splithttpSettings", JSONObject().apply {
                put("path", p.path.ifEmpty { "/" })
                if (p.host.isNotEmpty()) put("host", p.host)
            })

            "tcp" -> if (p.host.isNotEmpty() && p.security == "none") {
                put("tcpSettings", JSONObject().apply {
                    put("header", JSONObject().apply {
                        put("type", "http")
                        put("request", JSONObject().apply {
                            put("headers", JSONObject().put("Host", JSONArray().put(p.host)))
                        })
                    })
                })
            }
        }

        when (p.security) {
            "tls" -> put("tlsSettings", JSONObject().apply {
                if (p.sni.isNotEmpty()) put("serverName", p.sni)
                if (p.alpn.isNotEmpty()) {
                    put("alpn", JSONArray(p.alpn.split(",").map { it.trim() }))
                }
                if (p.fingerprint.isNotEmpty()) put("fingerprint", p.fingerprint)
                if (p.host.isNotEmpty() && net == "ws") {
                    put("allowInsecure", false)
                }
            })

            "reality" -> put("realitySettings", JSONObject().apply {
                if (p.sni.isNotEmpty()) put("serverName", p.sni)
                if (p.publicKey.isNotEmpty()) put("publicKey", p.publicKey)
                if (p.shortId.isNotEmpty()) put("shortId", p.shortId)
                if (p.fingerprint.isNotEmpty()) put("fingerprint", p.fingerprint)
                if (p.spiderX.isNotEmpty()) put("spiderX", p.spiderX)
            })
        }
    }
}
