package subtext.yuvallovenotes.lovelettersoverview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.R
import java.lang.ref.WeakReference

class LetterListSwipeCallback(context: Context, private val onItemSwiped: OnItemSwipe) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    private var icon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_sweep_white_36dp)!!
    private var background: ColorDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.medium_purple))
    private val weakContext: WeakReference<Context> = WeakReference(context)

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.ACTION_STATE_IDLE
        val swipeFlags = ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.layoutPosition
        onItemSwiped.onSwiped(position)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView: View = viewHolder.itemView
        val backgroundCornerOffset = 0 //so background is behind the rounded corners of itemView

        val iconMargin: Int = icon.intrinsicWidth / 2
        val iconTop: Int = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        when {

            dX > 0 -> {
                icon = ContextCompat.getDrawable(weakContext.get()!!, R.drawable.ic_delete_sweep_white_36dp)!!
                background = ColorDrawable(ContextCompat.getColor(weakContext.get()!!, R.color.medium_purple))
                val iconLeft: Int = itemView.left + iconMargin
                val iconRight: Int = itemView.left + iconMargin + icon.intrinsicWidth
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom)
            }

              dX < 0 -> { // Swiping to the right
                return
            }

            else -> { // view is unSwiped
                background.setBounds(0, 0, 0, 0)
            }
        }

        background.draw(c)
        icon.draw(c)
    }
}