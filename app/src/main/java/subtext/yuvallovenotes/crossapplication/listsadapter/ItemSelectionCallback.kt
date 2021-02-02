package subtext.yuvallovenotes.crossapplication.listsadapter

/**
 * Callback for items selection within a recycler view
 * */
interface ItemSelectionCallback {

    /**
     * Called after an item was added to the selected items list
     * */
    fun onItemSelected()

    /**
     * Called before an item will be removed from the selected items list
     * */
    fun itemWillBeRemovedFromSelectionList()
}