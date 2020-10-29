package cn.mgdream.webssh.core

import com.jcraft.jsch.UserInfo

open class UserInfoAdapter : UserInfo {
    override fun getPassphrase(): String {
        TODO("Not yet implemented")
    }

    override fun getPassword(): String {
        TODO("Not yet implemented")
    }

    override fun promptPassword(message: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun promptPassphrase(message: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun promptYesNo(message: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun showMessage(message: String?) {
        TODO("Not yet implemented")
    }

}
