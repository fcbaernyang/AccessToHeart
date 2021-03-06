package access.to.heart.fragment

import access.to.heart.Bean.ProfileUser
import access.to.heart.Bean.User
import access.to.heart.HTTPAround.MyStringObserver
import access.to.heart.HTTPAround.MyTemplateObserver
import access.to.heart.LoginActivity
import access.to.heart.PersonActivity
import access.to.heart.R
import access.to.heart.utils.GlobalOptions
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * Project AccessToHeart.
 * Created by 旭 on 2017/7/12.
 */
class ProfileFragment : BaseFragment() {

    private var userId: Int = 0

    override fun init() {

        userId = arguments["id"] as Int

        add.setOnClickListener {
            val et: EditText = EditText(activity)
            AlertDialog.Builder(activity).setTitle("输入档案名称")
                    .setView(et)
                    .setPositiveButton("添加", { _, _ -> addProfileToApi(et.text.toString()) })
                    .show()
        }

        logoff.setOnClickListener {
            clearSharedPreferences()
            GlobalOptions.nowProfileId = 0
            startActivity(Intent(activity, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        pie.setOnClickListener {
            startActivity(Intent(activity, PersonActivity::class.java))
        }

        delete.setOnClickListener {
            cloudAPI.deleteProfileById(GlobalOptions.nowProfileId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyTemplateObserver<String>() {
                        override fun onNext(t: String) {
                            Toast.makeText(activity, "删除成功", Toast.LENGTH_SHORT).show()
                            getProfileFromApi()
                        }
                    })
        }

        getProfileFromApi()
    }

    private fun addProfileToApi(profileName: String) {
        cloudAPI.postProfile(ProfileUser(ProfileName = profileName,
                User = User(Id = userId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MyStringObserver() {
                    override fun onNext(t: String) {
                        super.onNext(t)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "添加档案成功", Toast.LENGTH_SHORT).show()
                        getProfileFromApi()
                    }
                })
    }

    private fun clearSharedPreferences() =
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()

    private fun getProfileFromApi() {
        cloudAPI.getProfile(query = "User.Id:$userId")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MyTemplateObserver<List<ProfileUser>>() {
                    override fun onError(e: Throwable) {
                        addProfileToApi("default")
                    }

                    override fun onNext(t: List<ProfileUser>) {
//
//                        if (t.isEmpty()) {
//
//                            return
//                        }

                        GlobalOptions.nowProfileId = t[0].Id

                        val mAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, t.map { it -> it.ProfileName })

                        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        spinner.apply {
                            adapter = mAdapter

                            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                    GlobalOptions.nowProfileId = t[p2].Id
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }
                            }
                        }

                        mAdapter.notifyDataSetChanged()
                    }
                })
    }

    override val layoutId: Int
        get() = R.layout.fragment_profile
}