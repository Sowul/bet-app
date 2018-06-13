package info.czekanski.bet.domain.home

import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.czekanski.bet.R
import info.czekanski.bet.domain.home.cells.*
import info.czekanski.bet.domain.home.view_holder.*
import info.czekanski.bet.misc.Cell

class MatchesAdapter(
        private val callback: Callback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var cells: List<Cell> = listOf()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_WELCOME -> WelcomeViewHolder(parent.inflate(R.layout.holder_home_welcome))
        TYPE_HEADER -> HeaderViewHolder(parent.inflate(R.layout.holder_home_header))
        TYPE_MATCH -> MatchViewHolder(parent.inflate(R.layout.holder_home_match), callback)
        TYPE_BET -> BetViewHolder(parent.inflate(R.layout.holder_home_bet), callback)
        TYPE_LOADER -> StaticViewHolder(parent.inflate(R.layout.holder_home_loader))
        else -> throw RuntimeException("Unknown viewType $viewType for MatchesAdapter")
    }

    override fun getItemCount() = cells.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is WelcomeViewHolder -> holder.bind(cells[position] as WelcomeCell)
        is HeaderViewHolder -> holder.bind(cells[position] as HeaderCell)
        is MatchViewHolder -> holder.bind(cells[position] as MatchCell)
        is BetViewHolder -> holder.bind(cells[position] as BetCell)
        else -> {
            //throw RuntimeException("Unknown viewholder for position $position")
        }
    }

    override fun getItemId(position: Int): Long{
        val cell = cells[position]
        return when(cell) {
            is WelcomeCell -> -1000
            is HeaderCell -> -1001
            is LoaderCell -> -1002
            is MatchCell -> cell.match.id.hashCode().toLong()
            is BetCell -> cell.bet.id.hashCode().toLong()
            else -> 0
        }
    }

    override fun getItemViewType(position: Int) = when(cells[position]) {
        is WelcomeCell -> TYPE_WELCOME
        is HeaderCell -> TYPE_HEADER
        is MatchCell -> TYPE_MATCH
        is BetCell -> TYPE_BET
        is LoaderCell -> TYPE_LOADER
        else -> throw RuntimeException("Unknown viewtype for position $position")
    }

    fun setCells(new: List<Cell>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback(){
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return cells[oldItemPosition].hashCode() == new[newItemPosition].hashCode()
            }

            override fun getOldListSize() = cells.size

            override fun getNewListSize() = new.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return cells[oldItemPosition] == new[newItemPosition]
            }
        }).dispatchUpdatesTo(this)
        this.cells = new
    }

    companion object {
        const val TYPE_WELCOME = 1
        const val TYPE_HEADER = 2
        const val TYPE_MATCH = 3
        const val TYPE_BET = 4
        const val TYPE_LOADER = 5
    }

    private fun ViewGroup.inflate(@LayoutRes layout: Int): View =
            LayoutInflater.from(context).inflate(layout, this, false)
}

typealias Callback = (Cell) -> Unit