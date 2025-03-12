package com.mau.exploreai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mau.exploreai.databinding.FragmentTutorialBinding

// TutorialFragment.kt
class TutorialFragment : Fragment() {
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val page = it.getParcelable<TutorialPage>(ARG_PAGE)
            binding.titleText.text = page?.title
            binding.descriptionText.text = page?.description
        }
    }

    companion object {
        private const val ARG_PAGE = "page"

        fun newInstance(page: TutorialPage) = TutorialFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PAGE, page)
            }
        }
    }
}
