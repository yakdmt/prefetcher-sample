package xyz.yakdmt.prefetcherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import xyz.yakdmt.prefetcherapp.list.HeavyListFragment
import xyz.yakdmt.prefetcherapp.list.HeavyListFragment.Companion.KEY_GAPWORKER_ENABLED
import xyz.yakdmt.prefetcherapp.list.HeavyListFragment.Companion.KEY_PREFETCHER_ENABLED

class MainFragment : Fragment() {

    private lateinit var goToListButton: Button
    private lateinit var usePrefetcherSwitch: Switch
    private lateinit var useGapworkerSwitch: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.fragment_main, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goToListButton = view.findViewById(R.id.go_to_list_button)
        usePrefetcherSwitch = view.findViewById(R.id.use_prefetcher_switch)
        useGapworkerSwitch = view.findViewById(R.id.use_gapworker_switch)

        goToListButton.setOnClickListener {
            val listFragment = HeavyListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_PREFETCHER_ENABLED, usePrefetcherSwitch.isChecked)
                    putBoolean(KEY_GAPWORKER_ENABLED, useGapworkerSwitch.isChecked)
                }
            }

            activity?.supportFragmentManager?.beginTransaction()?.let {
                it.replace(R.id.fragment_container, listFragment)
                it.addToBackStack(null)
                it.commit()
            }
        }
    }
}