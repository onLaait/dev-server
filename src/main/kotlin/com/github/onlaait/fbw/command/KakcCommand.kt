package com.github.onlaait.fbw.command

import com.github.onlaait.fbw.command.argument.ArgumentText
import com.github.onlaait.fbw.system.Kakc
import com.github.onlaait.fbw.utils.alertMsg
import com.github.onlaait.fbw.utils.errorMsg
import com.github.onlaait.fbw.utils.infoMsg
import com.github.onlaait.fbw.utils.sendMsg
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.entity.Player

object KakcCommand : Command("kakc") {
    init {
        val MSG_CHKEY_SUCCESS = "한/영 전환 문자를 '%s'(으)로 설정했습니다."
        val MSG_CHKEY_INVALID = "사용할 수 없는 문자입니다."
        val MSG_CHMOD_SUCCESS = "모드를 [%s](으)로 설정했습니다."

        setDefaultExecutor { sender, context ->
            val cmd = context.commandName
            val chmods = Kakc.ChangeMode.entries
            sender.sendMsg(usage(
                "$cmd help - 한/영 입력 방법을 봅니다.",
                "$cmd chkey <문자> - 한/영 전환 문자를 설정합니다.",
                "$cmd chmod <0~${chmods.lastIndex}> - 모드를 설정합니다. (${chmods.joinToString(" / ") { "${it.ordinal}: ${it.detail}" }})"
            ))
        }

        val argHelp = ArgumentLiteral("help")

        val argChkey = ArgumentLiteral("chkey")
        val argChkeyChar = ArgumentText("문자")

        val argChmod = ArgumentLiteral("chmod")
        val argChmodNum = ArgumentWord("모드").from(*Kakc.ChangeMode.entries.map { it.ordinal.toString() }.toTypedArray())

        addSyntax({ sender, _ ->
            sender.infoMsg(
                "* 한/영 전환 문자가 [\"]라고 할 때 입력 방법입니다.",
                "한글 입력하기: [skfnxh tktmzp] -> [나루토 사스케]",
                "한/영 전환하기: [emflald \"chu chu] -> [드리밍 chu chu]",
                "한/영 전환 문자 그냥 입력하기: [sks \"\"qlxj\"\"ek] -> [난 \"비터\"다]"
            )
        }, argHelp)

        addSyntax({ sender, context ->
            val str = context[argChkeyChar]
            val c by lazy { str[0] }
            if (str.length != 1 || c in 'A'..'z' || c in 'ㄱ'..'ㅣ' || c in '가'..'힣' || c == '\\') {
                sender.errorMsg(MSG_CHKEY_INVALID)
                return@addSyntax
            }
            Kakc.playersChKey[(sender as Player).uuid] = c
            sender.alertMsg(String.format(MSG_CHKEY_SUCCESS, c))
        }, argChkey, argChkeyChar)

        addSyntax({ sender, context ->
            val mod = Kakc.ChangeMode.entries[context[argChmodNum].toInt()]
            Kakc.playersChMod[(sender as Player).uuid] = mod
            sender.alertMsg(String.format(MSG_CHMOD_SUCCESS, mod.detail))
        }, argChmod, argChmodNum)
    }
}