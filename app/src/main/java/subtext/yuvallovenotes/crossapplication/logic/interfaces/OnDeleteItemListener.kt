package arielmann.constantawarenesssupporter.crossapplication.logic.interfaces

interface OnDeleteItemListener<T> {
    fun onDeleteRequired(id : String)
}
