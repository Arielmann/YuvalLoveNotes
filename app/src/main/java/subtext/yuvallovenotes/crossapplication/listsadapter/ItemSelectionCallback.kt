package subtext.yuvallovenotes.crossapplication.listsadapter

import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter

/**
 * Callback for items selection within a recycler view
 * */
interface ItemSelectionCallback<T> {

    /**
     * Called after an item was added to the selected items list
     * */
    fun onItemSelected(item: T)

    /**
     * Called before an item will be removed from the selected items list
     * */
    fun itemWillBeRemovedFromSelectionList(item: T)
}