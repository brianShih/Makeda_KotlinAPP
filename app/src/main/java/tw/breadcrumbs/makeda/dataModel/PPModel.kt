package tw.breadcrumbs.makeda.dataModel

data class PPModel (
    var id: Long,
    var cloudID: Long,
    var name: String,
    var phone: String,
    var country: String,
    var addr: String,
    var fb: String,
    var web: String,
    var blogInfo: String,
    var opentime: String,
    var tag_note: String,
    var descrip: String,
    var pic_url: String,
    var score: Int = 0,
    var status: Int = 0,
    var distance: Float = 0f,
    var calDistanceDone : Boolean = false
)