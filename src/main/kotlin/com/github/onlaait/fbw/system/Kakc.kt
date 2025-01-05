package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.server.eventHandler
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent
import java.util.*

object Kakc {

    init {
        eventHandler.addChild(
            EventNode.all("kakc").setPriority(-100).addListener(PlayerChatEvent::class.java) { e ->
                val player = e.player
                val uuid = player.uuid
                val msg = e.rawMessage
                val mod = playersChMod.getOrPut(uuid) { ChangeMode.DEFAULT }
                if ((mod == ChangeMode.DEFAULT && msg.none { it in 'ㄱ'..'ㅣ' || it in '가'..'힣' } || mod == ChangeMode.ALWAYS) && msg.any { it in 'A'..'z' }) {
                    val chKey = playersChKey.getOrPut(uuid) { '"' }
                    val changed = change(msg, chKey)
                    e.isCancelled = true
                    EventDispatcher.call(PlayerChatEvent(player, e.recipients, changed))
                }
            }
        )
    }

    enum class ChangeMode(val detail: String) {
        DISABLE("비활성화"),
        DEFAULT("한글을 포함하지 않는 경우에만(기본값)"),
        ALWAYS("항상")
    }

    val playersChMod = HashMap<UUID, ChangeMode>()
    val playersChKey = HashMap<UUID, Char>()

    private fun change(str: String, akey: Char): String {
        val ret = StringBuilder()
        val temp = StringBuilder()
        var lastChar: Char? = null
        var isHangeulMod = true
        for (c in str) {
            when (c) {
                akey -> {
                    if (lastChar == akey) {
                        ret.append(akey)
                    }
                    ret.append(if (isHangeulMod) engType2Kor(temp.toString()) else temp)
                    temp.clear()
                    isHangeulMod = !isHangeulMod
                }
                '\\' -> {
                    if (lastChar == '\\') {
                        ret.append('\\')
                    }
                    ret.append(if (isHangeulMod) engType2Kor(temp.toString()) else temp)
                    temp.clear()
                }
                else -> {
                    temp.append(c)
                }
            }
            lastChar = if ((lastChar != akey || c != akey) && (lastChar != '\\' || c != '\\')) {
                c
            } else {
                null
            }
        }
        return ret.append(if (isHangeulMod) engType2Kor(temp.toString()) else temp).toString()
    }

    private const val ENG_KEY = "rRseEfaqQtTdwWczxvgkoiOjpuPhynbml"
    private const val KOR_KEY = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎㅏㅐㅑㅒㅓㅔㅕㅖㅗㅛㅜㅠㅡㅣ"

    private const val CHO_LIST = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    private const val JUNG_LIST = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
    private const val JONG_LIST = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"

    private fun engType2Kor(src: String): String {
        var res = ""
        var nCho = -1
        var nJung = -1
        var nJong = -1

        for (c in src) {
            val p = ENG_KEY.indexOf(c)
                .takeIf { it != -1 } ?: ENG_KEY.indexOf(c.lowercaseChar())
            if (p == -1) {
                if (nCho != -1) {
                    res += if (nJung != -1)
                        makeHangul(nCho, nJung, nJong)
                    else
                        CHO_LIST[nCho]
                } else {
                    if (nJung != -1)
                        res += JUNG_LIST[nJung]
                    else if (nJong != -1)
                        res += JONG_LIST[nJong]
                }
                nCho = -1
                nJung = -1
                nJong = -1
                res += c
            } else if (p < 19) {
                if (nJung != -1) {
                    if (nCho == -1) {
                        res += JUNG_LIST[nJung]
                        nJung = -1
                        nCho = CHO_LIST.indexOf(KOR_KEY[p])
                    } else {
                        if (nJong == -1) {
                            nJong = JONG_LIST.indexOf(KOR_KEY[p])
                            if (nJong == -1) {
                                res += makeHangul(nCho, nJung, nJong)
                                nCho = CHO_LIST.indexOf(KOR_KEY[p])
                                nJung = -1
                            }
                        } else if (nJong == 0 && p == 9) {
                            nJong = 2
                        } else if (nJong == 3 && p == 12) {
                            nJong = 4
                        } else if (nJong == 3 && p == 18) {
                            nJong = 5
                        } else if (nJong == 7 && p == 0) {
                            nJong = 8
                        } else if (nJong == 7 && p == 6) {
                            nJong = 9
                        } else if (nJong == 7 && p == 7) {
                            nJong = 10
                        } else if (nJong == 7 && p == 9) {
                            nJong = 11
                        } else if (nJong == 7 && p == 16) {
                            nJong = 12
                        } else if (nJong == 7 && p == 17) {
                            nJong = 13
                        } else if (nJong == 7 && p == 18) {
                            nJong = 14
                        } else if (nJong == 16 && p == 9) {
                            nJong = 17
                        } else {
                            res += makeHangul(nCho, nJung, nJong)
                            nCho = CHO_LIST.indexOf(KOR_KEY[p])
                            nJung = -1
                            nJong = -1
                        }
                    }
                } else {
                    if (nCho == -1) {
                        if (nJong != -1) {
                            res += JONG_LIST[nJong]
                            nJong = -1
                        }
                        nCho = CHO_LIST.indexOf(KOR_KEY[p])
                    } else if (nCho == 0 && p == 9) {
                        nCho = -1
                        nJong = 2
                    } else if (nCho == 2 && p == 12) {
                        nCho = -1
                        nJong = 4
                    } else if (nCho == 2 && p == 18) {
                        nCho = -1
                        nJong = 5
                    } else if (nCho == 5 && p == 0) {
                        nCho = -1
                        nJong = 8
                    } else if (nCho == 5 && p == 6) {
                        nCho = -1
                        nJong = 9
                    } else if (nCho == 5 && p == 7) {
                        nCho = -1
                        nJong = 10
                    } else if (nCho == 5 && p == 9) {
                        nCho = -1
                        nJong = 11
                    } else if (nCho == 5 && p == 16) {
                        nCho = -1
                        nJong = 12
                    } else if (nCho == 5 && p == 17) {
                        nCho = -1
                        nJong = 13
                    } else if (nCho == 5 && p == 18) {
                        nCho = -1
                        nJong = 14
                    } else if (nCho == 7 && p == 9) {
                        nCho = -1
                        nJong = 17
                    } else {
                        res += CHO_LIST[nCho]
                        nCho = CHO_LIST.indexOf(KOR_KEY[p])
                    }
                }
            } else {
                if (nJong != -1) {
                    val newCho: Int
                    when (nJong) {
                        2 -> {
                            nJong = 0
                            newCho = 9
                        }
                        4 -> {
                            nJong = 3
                            newCho = 12
                        }
                        5 -> {
                            nJong = 3
                            newCho = 18
                        }
                        8 -> {
                            nJong = 7
                            newCho = 0
                        }
                        9 -> {
                            nJong = 7
                            newCho = 6
                        }
                        10 -> {
                            nJong = 7
                            newCho = 7
                        }
                        11 -> {
                            nJong = 7
                            newCho = 9
                        }
                        12 -> {
                            nJong = 7
                            newCho = 16
                        }
                        13 -> {
                            nJong = 7
                            newCho = 17
                        }
                        14 -> {
                            nJong = 7
                            newCho = 18
                        }
                        17 -> {
                            nJong = 16
                            newCho = 9
                        }
                        else -> {
                            newCho = CHO_LIST.indexOf(JONG_LIST[nJong])
                            nJong = -1
                        }
                    }
                    res += if (nCho != -1)
                        makeHangul(nCho, nJung, nJong)
                    else
                        JONG_LIST[nJong]

                    nCho = newCho
                    nJung = -1
                    nJong = -1
                }
                if (nJung == -1) {
                    nJung = JUNG_LIST.indexOf(KOR_KEY[p])
                } else if (nJung == 8 && p == 19) {
                    nJung = 9
                } else if (nJung == 8 && p == 20) {
                    nJung = 10
                } else if (nJung == 8 && p == 32) {
                    nJung = 11
                } else if (nJung == 13 && p == 23) {
                    nJung = 14
                } else if (nJung == 13 && p == 24) {
                    nJung = 15
                } else if (nJung == 13 && p == 32) {
                    nJung = 16
                } else if (nJung == 18 && p == 32) {
                    nJung = 19
                } else {
                    if (nCho != -1) {
                        res += makeHangul(nCho, nJung, nJong)
                        nCho = -1
                    } else
                        res += JUNG_LIST[nJung]
                    nJung = -1
                    res += KOR_KEY[p]
                }
            }
        }

        if (nCho != -1) {
            res += if (nJung != -1)
                makeHangul(nCho, nJung, nJong)
            else
                CHO_LIST[nCho]
        } else {
            if (nJung != -1)
                res += JUNG_LIST[nJung]
            else {
                if (nJong != -1)
                    res += JONG_LIST[nJong]
            }
        }

        return res
    }

    private fun makeHangul(nCho: Int, nJung: Int, nJong: Int): Char =
        Char(0xAC00 + nCho * 21 * 28 + nJung * 28 + nJong + 1)
}