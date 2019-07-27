package xyz.yakdmt.prefetcherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import xyz.yakdmt.prefetcherapp.list.HeavyListFragment

class MainFragment : Fragment() {

    private lateinit var goToListButton: Button
    private lateinit var usePrefetcherSwitch: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.fragment_main, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goToListButton = view.findViewById(R.id.go_to_list_button)
        usePrefetcherSwitch = view.findViewById(R.id.use_prefetcher_switch)

        goToListButton.setOnClickListener {
            val listFragment = HeavyListFragment()

            activity?.supportFragmentManager?.beginTransaction()?.let {
                it.replace(R.id.fragment_container, listFragment)
                it.addToBackStack(null)
                it.commit()
            }
        }
    }
}