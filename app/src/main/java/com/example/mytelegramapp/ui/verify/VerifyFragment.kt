package com.example.mytelegramapp.ui.verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mytelegramapp.databinding.FragmentVerifyBinding
import com.example.mytelegramapp.ui.MainViewModel

class VerifyFragment : Fragment() {

    private var _binding: FragmentVerifyBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.Factory(requireActivity().application)
    }
    private lateinit var verifyAdapter: VerifyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        mainViewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            verifyAdapter.updateData(sessions)
        }

        binding.verifyAllButton.setOnClickListener {
            mainViewModel.verifyAllSessions()
        }
    }

    private fun setupRecyclerView() {
        verifyAdapter = VerifyAdapter(emptyList())
        binding.verifyRecyclerView.apply {
            adapter = verifyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
