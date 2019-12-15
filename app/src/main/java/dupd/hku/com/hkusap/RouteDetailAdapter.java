package dupd.hku.com.hkusap;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dupd.hku.com.hkusap.base.BaseAdapter;
import dupd.hku.com.hkusap.base.BaseViewHolder;
import dupd.hku.com.hkusap.manager.SPTeleprompter;
import dupd.hku.com.hkusap.manager.SPTeleprompterGroup;
import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.model.IEnum.SPWeightType;

import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

public class RouteDetailAdapter extends BaseAdapter<SPTeleprompterGroup> {

    private SPTeleprompter mTeleprompter;

    public RouteDetailAdapter(SPTeleprompter teleprompter) {
        super(teleprompter.routeGroups());
        mTeleprompter = teleprompter;
    }

    @Override
    public BaseViewHolder<SPTeleprompterGroup> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_detail, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends BaseViewHolder<SPTeleprompterGroup> implements IEnum {
        private final Resources mResources;
        @BindView(R.id.iv_left)
        ImageView mIvLeft;
        @BindView(R.id.tv_title)
        TextView mTvTitle;
        @BindView(R.id.tv_describe)
        TextView mTvDescribe;
        @BindView(R.id.iv_right)
        ImageView mIvRight;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mResources = itemView.getContext().getResources();
        }

        @Override
        public void bindPosition(final int position, final SPTeleprompterGroup group) {

            mTvTitle.setText(mTeleprompter.directionDescriptionAtIndex(position));

            mTvDescribe.setText(mTeleprompter.subtitleDescriptionAtIndex(position));

            mIvLeft.setImageResource(mTeleprompter.directionImageAtIndex(position));
            mIvRight.setImageResource(imageForType(group.groupType()));
        }


        public int colorForType(SPWeightType type) {
            switch (type) {
                case SPWeightTypeDoor:
                case SPWeightTypeLift:
                case SPWeightTypeEntrance:
                case SPWeightTypeEscalator:
                    return mResources.getColor(R.color.white_60);
                case SPWeightTypeOthers:
                default:
                    return mResources.getColor(R.color.transparent);
            }
        }
    }

}
