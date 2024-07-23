package edu.vt.cs5254.dreamcatcher

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding

class DreamEntryHolder(private val binding: ListItemDreamEntryBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var boundEntry: DreamEntry
        private set // sus

    fun bind(dreamEntry: DreamEntry) {
        boundEntry = dreamEntry
        binding.dreamEntryButton.configureForEntry(dreamEntry)
    }
}
class DreamEntryAdapter(private val entryList: List<DreamEntry>) : RecyclerView.Adapter<DreamEntryHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamEntryBinding.inflate(layoutInflater, parent,  false)
        return DreamEntryHolder(binding)
    }

    override fun getItemCount(): Int {
        return entryList.size
    }

    override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
        holder.bind(entryList[position])
    }
}