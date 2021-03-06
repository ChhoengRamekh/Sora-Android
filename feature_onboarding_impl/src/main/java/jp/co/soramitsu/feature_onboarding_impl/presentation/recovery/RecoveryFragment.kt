/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.feature_onboarding_impl.presentation.recovery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import jp.co.soramitsu.common.base.BaseFragment
import jp.co.soramitsu.common.presentation.DebounceClickHandler
import jp.co.soramitsu.common.presentation.view.DebounceClickListener
import jp.co.soramitsu.common.presentation.view.SoraProgressDialog
import jp.co.soramitsu.core_di.holder.FeatureUtils
import jp.co.soramitsu.feature_onboarding_api.di.OnboardingFeatureApi
import jp.co.soramitsu.feature_onboarding_impl.R
import jp.co.soramitsu.feature_onboarding_impl.di.OnboardingFeatureComponent
import jp.co.soramitsu.feature_onboarding_impl.presentation.OnboardingRouter
import kotlinx.android.synthetic.main.fragment_recovery.mnemonic_input
import kotlinx.android.synthetic.main.fragment_recovery.nextBtn
import kotlinx.android.synthetic.main.fragment_recovery.toolbar
import javax.inject.Inject

class RecoveryFragment : BaseFragment<RecoveryViewModel>() {

    @Inject lateinit var debounceClickHandler: DebounceClickHandler

    private lateinit var progressDialog: SoraProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recovery, container, false)
    }

    override fun initViews() {
        progressDialog = SoraProgressDialog(activity!!)

        toolbar.setHomeButtonListener { viewModel.backButtonClick() }

        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mnemonic_input, 0)

        mnemonic_input.setOnEditorActionListener { _, _, _ ->
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(mnemonic_input.windowToken, 0)
            true
        }
    }

    override fun subscribe(viewModel: RecoveryViewModel) {
        nextBtn.setOnClickListener(DebounceClickListener(debounceClickHandler) {
            viewModel.btnNextClick(mnemonic_input.text.toString())
        })

        observe(viewModel.getProgressVisibility(), Observer {
            if (it) progressDialog.show() else progressDialog.dismiss()
        })
    }

    override fun inject() {
        FeatureUtils.getFeature<OnboardingFeatureComponent>(context!!, OnboardingFeatureApi::class.java)
            .recoveryComponentBuilder()
            .withFragment(this)
            .withRouter(activity as OnboardingRouter)
            .build()
            .inject(this)
    }

    override fun onResume() {
        activity!!.window.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        super.onResume()
    }
}
