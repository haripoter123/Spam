package com.example.mytelegramapp.ui.sessions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mytelegramapp.databinding.FragmentSessionsBinding
import com.example.mytelegramapp.ui.MainViewModel

class SessionsFragment : Fragment() {

    private var _binding: FragmentSessionsBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.Factory(requireActivity().application)
    }
    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        mainViewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            sessionsAdapter.updateData(sessions)
        }

        binding.downloadSessionsButton.setOnClickListener {
            mainViewModel.downloadSessions()
        }
    }

    private fun setupRecyclerView() {
        sessionsAdapter = SessionsAdapter(emptyList())
        binding.sessionsRecyclerView.apply {
            adapter = sessionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
