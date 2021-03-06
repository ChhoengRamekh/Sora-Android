/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.core_db.model

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectDetailsWithGalleryLocal(
    @Embedded var projectLocal: ProjectDetailsLocal
) {

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    var gallery: List<GalleryItemLocal> = mutableListOf()
}