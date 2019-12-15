package dupd.hku.com.hkusap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dupd.hku.com.hkusap.base.BaseAdapter;
import dupd.hku.com.hkusap.base.BaseViewHolder;
import dupd.hku.com.hkusap.model.Event;

public class EventAdapter extends BaseAdapter<Event> {
    @Override
    public BaseViewHolder<Event> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }
    public class ViewHolder extends BaseViewHolder<Event> {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_exam)
        TextView tvExam;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindPosition(int position, Event event) {
            tvName.setText(event.DESCRIPTION);
            tvTime.setText(event.START_TIME + "-" + event.END_TIME);
            tvExam.setText("[" + event.DEPT_CODE + "]");
        }
    }
}
