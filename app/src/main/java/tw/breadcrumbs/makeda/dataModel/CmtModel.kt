package tw.breadcrumbs.makeda.dataModel

data class CmtModel (
    var commentID: Int,
    var comment_post_ID: Int,
    var comment_parent_ID: Int,
    var comment_author: String,
    var comment_author_email: String,
    var comment_content: String,
    var comment_date: String
)