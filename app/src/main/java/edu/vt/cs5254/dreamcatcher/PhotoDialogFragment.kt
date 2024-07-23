package edu.vt.cs5254.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.criminalintent.getScaledBitmap
import edu.vt.cs5254.dreamcatcher.databinding.FragmentPhotoDialogBinding
import java.io.File

class PhotoDialogFragment : DialogFragment() {

    private val args: PhotoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Feature 6
        val binding = FragmentPhotoDialogBinding.inflate(layoutInflater)

        val file = File(
            requireActivity().filesDir,
            args.dreamPhotoFilename
        )


        binding.root.doOnLayout { dialogRoot ->
           val bitmap = getScaledBitmap(file.path, dialogRoot.width, dialogRoot.height)
            binding.photoDetail.setImageBitmap(bitmap)
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()

    }

}