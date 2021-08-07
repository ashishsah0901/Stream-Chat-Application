package com.example.streamchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.streamchat.ui.viewmodels.ChannelViewModel
import com.example.streamchat.R
import com.example.streamchat.databinding.FragmentChannelBinding
import com.example.streamchat.util.navigateSafely
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChannelFragment : Fragment() {

    private var _binding: FragmentChannelBinding? = null
    private val binding: FragmentChannelBinding
        get() = _binding!!

    private val viewModel : ChannelViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = viewModel.getUser()
        if(user == null) {
            findNavController().popBackStack()
            return
        }
        val factory = ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging")
            ),
            sort = ChannelListViewModel.DEFAULT_SORT,
            limit = 30
        )
        val channelViewModel : ChannelListViewModel by viewModels{ factory }
        val channelHeaderViewBinding : ChannelListHeaderViewModel by viewModels()
        channelViewModel.bindView(binding.channelListView, viewLifecycleOwner)
        channelHeaderViewBinding.bindView(binding.channelListHeaderView, viewLifecycleOwner)
        binding.channelListHeaderView.setOnUserAvatarClickListener {
            viewModel.logout()
            findNavController().popBackStack()
        }
        binding.channelListHeaderView.setOnActionButtonClickListener{
            findNavController().navigateSafely(R.id.action_channelFragment_to_createChannelDialog)
        }
        binding.channelListView.setChannelItemClickListener{
            findNavController().navigateSafely(R.id.action_channelFragment_to_chatFragment,Bundle().apply { putString("channelId",it.cid) })
        }
        lifecycleScope.launchWhenStarted {
            viewModel.createChannelEvent.collect { event ->
                when(event) {
                    is ChannelViewModel.CreateChannelEvent.Error -> {
                        Toast.makeText(requireContext(), event.error, Toast.LENGTH_SHORT).show()
                    }
                    is ChannelViewModel.CreateChannelEvent.Success -> {
                        Toast.makeText(requireContext(), R.string.channel_created, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.let {
                it.moveTaskToBack(true)
                it.finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}