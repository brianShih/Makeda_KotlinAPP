package tw.breadcrumbs.makeda.dataModel

data class TripListModel (
    var id: Long,
    var planname: String,
    var author: String,
    var grouplist: String,
    var contentJsonFormat: String,
    var cloudContent: String,
    var tripplanID: String,
    var log: String,
    var updated: Int
)