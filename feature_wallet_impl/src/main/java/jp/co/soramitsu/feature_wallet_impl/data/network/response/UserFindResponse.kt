/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.feature_wallet_impl.data.network.response

import com.google.gson.annotations.SerializedName
import jp.co.soramitsu.core_network_api.data.dto.StatusDto
import jp.co.soramitsu.feature_wallet_impl.data.network.model.AccountRemote

data class UserFindResponse(
    @SerializedName("status") val status: StatusDto,
    @SerializedName("results") val results: List<AccountRemote>
)