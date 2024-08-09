package tw.breadcrumbs.makeda

import android.content.Context
import tw.breadcrumbs.makeda.dataModel.PPModel

open class ListAction() {
    public open fun openImageView(imgViewer:ImageViewer_Fragment) {}

    public open fun pp_onClick(partItem: PPModel) {}

    public open fun onSwiped(position: Int) {}

}