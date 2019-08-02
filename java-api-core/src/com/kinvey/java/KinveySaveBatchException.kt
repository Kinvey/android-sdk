package com.kinvey.java

import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveyUpdateSingleItemError
import java.io.IOException

class KinveySaveBatchException(
    var errors: List<KinveyBatchInsertError>?,
    var putErrors: List<KinveyUpdateSingleItemError>?,
    var entities: List<*>?) : IOException() {
    val haveErrors: Boolean
        get () {
            return errors?.isNotEmpty() == true || putErrors?.isNotEmpty() == true
        }
}
