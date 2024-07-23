package edu.vt.cs5254.dreamcatcher
import android.content.ClipData.Item
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.criminalintent.getScaledBitmap
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "DreamDetailFragment"

class DreamDetailFragment: Fragment() {

    private val arg: DreamDetailFragmentArgs by navArgs()

    private val vm: DreamDetailViewModel by viewModels() {
        DreamDetailViewModelFactory(arg.dreamId)
    }

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "FragmentDetailBinding is null"
        }

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { tookPicture ->
        Log.w("DDF!!!", "Took picture? $tookPicture")
        if (tookPicture) {
            vm.dream.value?.let { dream ->
                binding.dreamPhoto.tag = null
                updatePhoto(dream)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)

        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)
        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)

        requireActivity().addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.fragment_dream_detail, menu)

                    // If no camera app available then hide take photo menu item
                    if (!canResolve()) {
                        menu.findItem(R.id.take_photo_menu).isVisible = false
                    }
                }


                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.take_photo_menu -> {
                            vm.dream.value?.let { dream ->
                                val photoFile = File(
                                    requireActivity().filesDir,
                                    dream.photoFileName
                                )

                                val photoUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "edu.vt.cs5254.dreamcatcher.fileprovider",
                                    photoFile
                                )
                                photoLauncher.launch(photoUri)
                            }
                            true

                        }
                        // Feature 4
                        R.id.share_dream_menu -> {
                            vm.dream.value?.let {
                                shareDream(it)
                            }
                            return true
                        }

                        else -> false
                    }

                }

            },
            viewLifecycleOwner
        )



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.dream.collect { dream ->
                    dream?.let { updateView(it) }

                }
            }
        }



        // Listeners/ Feature 6
        binding.dreamPhoto.setOnClickListener {
            vm.dream.value?.let { dream ->
                findNavController().navigate(DreamDetailFragmentDirections.showDetail(dream.photoFileName))
            }
        }

        binding.titleText.doOnTextChanged { text, _, _, _ ->
            vm.updateDream { oldDream ->
                oldDream.copy(title = text.toString())
                    .apply { entries = oldDream.entries }
            }
        }

        binding.addReflectionButton.setOnClickListener {
            findNavController().navigate(DreamDetailFragmentDirections.addReflection())
        }

        setFragmentResultListener(ReflectionDialogFragment.REQUEST_KEY) { _, bundle ->
            val entryText = bundle.getString(ReflectionDialogFragment.BUNDLE_KEY) ?: ""
            vm.updateDream { oldDream ->
                oldDream.copy().apply {
                    entries = oldDream.entries + DreamEntry(
                        kind = DreamEntryKind.REFLECTION,
                        text = entryText,
                        dreamId = oldDream.id
                    )
                }
            }
        }

        // Set Listeners


        binding.deferredCheckbox.setOnClickListener() {
            vm.handleDeferredClick()

        }

        binding.fulfilledCheckbox.setOnClickListener() {
            vm.handleFulfilledClick() // update the model
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView(dream: Dream) {
        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(dream.entries)

        // Set checkbox state and enabled state
        binding.fulfilledCheckbox.isChecked = dream.isFulfilled
        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.deferredCheckbox.isEnabled = !dream.isFulfilled
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred

        if (binding.titleText.text.toString() != dream.title) {
            binding.titleText.setText(dream.title)
        }

        // Hide Button/ Show
        if (dream.isFulfilled) {
            binding.addReflectionButton.hide()
        } else {
            binding.addReflectionButton.show()
        }

        // Title
        val dateString =
            DateFormat.format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss a", dream.lastUpdated)
        binding.lastUpdatedText.text = dateString

        updatePhoto(dream)


    }

    // Photo Feature 5
    private fun updatePhoto(dream: Dream) {

        if (binding.dreamPhoto.tag != dream.photoFileName) {
            val photoFile = File(
                requireActivity().filesDir,
                dream.photoFileName
            )
            if (photoFile.exists()) {
                binding.dreamPhoto.isEnabled = true
                binding.dreamPhoto.doOnLayout { imgView ->
                    val bitmap = getScaledBitmap(
                        photoFile.path,
                        imgView.width,
                        imgView.height
                    )
                    binding.dreamPhoto.setImageBitmap(bitmap)
                    binding.dreamPhoto.tag = dream.photoFileName
                }
            } else {
                binding.dreamPhoto.setImageBitmap(null)
                binding.dreamPhoto.tag = null
                binding.dreamPhoto.isEnabled = false
            }

        }

    }

    //
    private fun canResolve(): Boolean {
        return photoLauncher.contract.createIntent(
            requireContext(),
            Uri.EMPTY
        ).resolveActivity(requireActivity().packageManager) != null
    }

    private fun getItemTouchHelper(): ItemTouchHelper {

        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val dreamEntryHolder = viewHolder as DreamEntryHolder
                val swipedEntry = dreamEntryHolder.boundEntry
                vm.updateDream { oldDream ->
                    oldDream.copy().apply {
                        entries = oldDream.entries.filter { it != swipedEntry }
                    }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dreamEntryHolder = viewHolder as DreamEntryHolder
                val swipedEntry = dreamEntryHolder.boundEntry
                return if (swipedEntry.kind == DreamEntryKind.REFLECTION) {
                    ItemTouchHelper.LEFT
                } else {
                    0
                }
            }
        })
    }

    //  Feature 4
//    private fun shareDream(dream: Dream) {
//        val dateString =
//            DateFormat.format("'Last updated' yyyy-MM-dd", dream.lastUpdated)
//        val reflectionsHeader = getString(R.string.reflections_header)
//        val reflections = dream.entries.filter { it.kind == DreamEntryKind.REFLECTION }
//        val reflectionsString =
//            reflections.joinToString(prefix = reflectionsHeader) { "\n * ${it.text}" }
//        val statusString = when {
//            dream.isDeferred -> "This dream has been Deferred."
//            dream.isFulfilled -> "This dream has been Fulfilled."
//            else -> " "
//        }
//
//        val message = "${dream.title}\n$dateString\n$reflectionsString\n$statusString"
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "text/plain"
//            putExtra(Intent.EXTRA_SUBJECT, dream.title)
//            putExtra(Intent.EXTRA_TEXT, message)
//        }
//
//        if (reflections.isEmpty()) {
//            binding.addReflectionButton.visibility = View.GONE
//        }
//
//        val chooserIntent = Intent.createChooser(
//            shareIntent,
//            getString(R.string.share_dream)
//        )
//        startActivity(chooserIntent)
//
//
//    }

    private fun shareDream(dream: Dream) {
        val dateString = DateFormat.format("'Last updated' yyyy-MM-dd", dream.lastUpdated)
        val reflectionsHeader = getString(R.string.reflections_header)
        val reflections = dream.entries.filter { it.kind == DreamEntryKind.REFLECTION }
        val reflectionsString = if (reflections.isNotEmpty()) {
            reflections.joinToString(prefix = reflectionsHeader) { "\n * ${it.text}" }
        } else {
            ""
        }
        val statusString = when {
            dream.isDeferred -> "This dream has been Deferred."
            dream.isFulfilled -> "This dream has been Fulfilled."
            else -> " "
        }

        val message = "${dream.title}\n$dateString$reflectionsString\n$statusString"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, dream.title)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        val chooserIntent = Intent.createChooser(
            shareIntent,
            getString(R.string.share_dream)
        )
        startActivity(chooserIntent)
    }


}

