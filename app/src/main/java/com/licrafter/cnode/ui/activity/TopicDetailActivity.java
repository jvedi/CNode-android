package com.licrafter.cnode.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.licrafter.cnode.R;
import com.licrafter.cnode.base.BaseActivity;
import com.licrafter.cnode.cache.UserCache;
import com.licrafter.cnode.model.TopicDetailModel;
import com.licrafter.cnode.model.entity.Reply;
import com.licrafter.cnode.model.entity.TAB;
import com.licrafter.cnode.model.entity.TopicDetail;
import com.licrafter.cnode.mvp.presenter.TopicDetailPresenter;
import com.licrafter.cnode.mvp.view.MvpView;
import com.licrafter.cnode.utils.DateUtils;
import com.licrafter.cnode.utils.ImageLoader;
import com.licrafter.cnode.utils.IntentUtils;
import com.licrafter.cnode.utils.SwipeRefreshUtils;
import com.licrafter.cnode.utils.TopicDividerDecoration;
import com.licrafter.cnode.widget.CNodeWebView;
import com.licrafter.cnode.widget.richTextView.RichTextView;
import com.makeramen.roundedimageview.RoundedImageView;

import butterknife.BindView;

/**
 * author: shell
 * date 2017/2/27 下午3:53
 **/
public class TopicDetailActivity extends BaseActivity implements MvpView, View.OnClickListener {

    private static final int REQ_REPLY = 0x112;

    @BindView(R.id.comments_recyclerview)
    RecyclerView mDetailRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.layout_bottom_sheet)
    View mBottomSheet;
    @BindView(R.id.comment_fab)
    FloatingActionButton mReplyFab;

    //header
    @BindView(R.id.iv_avatar)
    RoundedImageView iv_avatar;
    @BindView(R.id.iv_collect)
    ImageView iv_collect;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_created_at)
    TextView tv_created_at;
    @BindView(R.id.tv_visit_count)
    TextView tv_visit_count;
    @BindView(R.id.tv_tab)
    TextView tv_tab;
    @BindView(R.id.wv_content)
    CNodeWebView mk_content;

    private TopicDetailPresenter mPresenter = new TopicDetailPresenter();
    private DetailAdapter mAdapter;
    private BottomSheetBehavior mBehavior;
    private TopicDetailModel mDetail;
    private String mTopicId;
    private boolean mIsCollected;

    @Override
    public int getContentView() {
        return R.layout.activity_topic_detail;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mTopicId = getIntent().getStringExtra("topicId");
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_topic_detail);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        SwipeRefreshUtils.initStyle(mRefreshLayout);
        mAdapter = new DetailAdapter();
        mDetailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDetailRecyclerView.addItemDecoration(new TopicDividerDecoration(this));
        mDetailRecyclerView.setAdapter(mAdapter);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);
    }

    @Override
    public void setListeners() {
        mReplyFab.setOnClickListener(this);
        iv_collect.setOnClickListener(this);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.getTopicDetailById(mTopicId);
            }
        });
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mToolbar.setTitle(getString(R.string.title_topic_detail));
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        if (mDetail != null) {
                            mToolbar.setTitle(mDetail.getData().getTitle());
                        }
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        mDetailRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    mReplyFab.show();
                } else {
                    mReplyFab.hide();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mDetailRecyclerView.scrollToPosition(0);
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void bind() {
        mPresenter.attachView(this);
        mRefreshLayout.setRefreshing(true);
        mPresenter.getTopicDetailById(mTopicId);
    }

    @Override
    public void unBind() {

    }

    public static void start(BaseActivity activity, String topicId) {
        Intent intent = new Intent(activity, TopicDetailActivity.class);
        intent.putExtra("topicId", topicId);
        activity.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_topic_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                shareTopic();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFailed(Throwable e) {
        mRefreshLayout.setRefreshing(false);
    }

    public void notifyGetDetailSuccess(TopicDetailModel topicDetailModel) {
        mRefreshLayout.setRefreshing(false);
        mDetail = topicDetailModel;
        initHeader();
        mAdapter.notifyDataSetChanged();
    }

    private void initHeader() {
        TopicDetail detail = mDetail.getData();
        ImageLoader.loadUrl(iv_avatar, detail.getAuthor().getAvatar_url());
        tv_title.setText(detail.getTitle());
        tv_user_name.setText(detail.getAuthor().getLoginname());
        mk_content.loadHtml(detail.getContent());
        tv_created_at.setText(DateUtils.format(detail.getCreate_at()));
        tv_visit_count.setText(String.format(getString(R.string.visit_count), detail.getVisit_count()));
        tv_tab.setText(String.format(getString(R.string.tab_name), TAB.ValueOf(detail.getTab())));
        setCollect(mDetail.getData().getIs_collect());
    }

    private void setCollect(boolean collect) {
        mIsCollected = collect;
        iv_collect.setImageResource(collect ? R.mipmap.ic_collected : R.mipmap.ic_uncollected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_collect:
                mPresenter.makeCollected(!mIsCollected, mDetail.getData().getId());
                setCollect(!mIsCollected);
                break;
            case R.id.comment_fab:
                MarkdownEditActivity.createReply(this, REQ_REPLY, mDetail.getData().getId(), null, null);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQ_REPLY) {
            mPresenter.getTopicDetailById(mTopicId);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void shareTopic() {
        IntentUtils.shareTopic(this, mDetail.getData().getTitle(), mDetail.getData().getId());
    }

    public void makeUpFailed(String msg, int position) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        mDetail.getData().getReplies().get(position).getUps().remove(UserCache.getUserId());
        mAdapter.notifyItemChanged(position + 1);
    }

    private class DetailAdapter extends RecyclerView.Adapter {

        private static final int TYPE_HEADER = 0x001;
        private static final int TYPE_ITEM = 0x002;
        private static final int TYPE_EMPTY = 0x003;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEADER:
                    return new ReplyCountHolder(LayoutInflater.from(TopicDetailActivity.this).inflate(R.layout.item_reply_count, parent, false));
                case TYPE_ITEM:
                    final ReplyHolder reply = new ReplyHolder(LayoutInflater.from(TopicDetailActivity.this).inflate(R.layout.item_reply, parent, false));
                    reply.iv_reply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Reply repEntity = mDetail.getData().getReplies().get(reply.getAdapterPosition() - 1);
                            MarkdownEditActivity.createReply(TopicDetailActivity.this, REQ_REPLY, mDetail.getData().getId(), repEntity.getId(), repEntity.getAuthor().getLoginname());
                        }
                    });
                    reply.iv_up.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (UserCache.getUserId() != null) {
                                int position = reply.getAdapterPosition() - 1;
                                Reply repEntity = mDetail.getData().getReplies().get(position);
                                boolean up = repEntity.getUps().contains(UserCache.getUserId());
                                int ups_count = repEntity.getUps().size();
                                makeUp(!up, reply.iv_up);
                                if (up) {
                                    repEntity.getUps().remove(UserCache.getUserId());
                                    reply.tv_up_count.setText(String.valueOf(ups_count - 1));
                                } else {
                                    repEntity.getUps().add(UserCache.getUserId());
                                    reply.tv_up_count.setText(String.valueOf(ups_count + 1));
                                }
                                mPresenter.makeUp(repEntity.getId(), position);
                            } else {
                                startActivity(new Intent(TopicDetailActivity.this, LoginActivity.class));
                            }
                        }
                    });
                    return reply;
                case TYPE_EMPTY:
                    return new EmptyHolder(LayoutInflater.from(TopicDetailActivity.this).inflate(R.layout.item_empty, parent, false));
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ReplyHolder) {
                ReplyHolder reply = (ReplyHolder) holder;
                Reply rep = mDetail.getData().getReplies().get(position - 1);
                ImageLoader.loadUrl(reply.iv_avatar, rep.getAuthor().getAvatar_url());
                reply.tv_user_name.setText(rep.getAuthor().getLoginname());
                reply.tv_created_at.setText(DateUtils.format(rep.getCreate_at()));
                reply.tv_content.setRichText(rep.getContent());
                reply.tv_up_count.setText(String.valueOf(rep.getUps().size()));
                if (UserCache.getUserId() != null) {
                    makeUp(rep.getUps().contains(UserCache.getUserId()), reply.iv_up);
                }
            } else if (holder instanceof ReplyCountHolder) {
                ((ReplyCountHolder) holder).tv_reply_count.setText(String.format(getString(R.string.reply_count), mDetail.getData().getReplies().size()));
            }
        }

        @Override
        public int getItemCount() {
            if (mDetail != null) {
                if (mDetail.getData().getReplies().size() != 0) {
                    return mDetail.getData().getReplies().size() + 1;
                } else {
                    return 2;
                }
            } else {
                return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else {
                if (mDetail.getData().getReplies().size() != 0) {
                    return TYPE_ITEM;
                } else {
                    return TYPE_EMPTY;
                }
            }
        }

        private void makeUp(boolean up, ImageView imageView) {
            imageView.setImageResource(up ? R.mipmap.ic_up : R.mipmap.ic_unup);
        }
    }

    private class ReplyHolder extends RecyclerView.ViewHolder {

        RoundedImageView iv_avatar;
        TextView tv_user_name;
        TextView tv_created_at;
        RichTextView tv_content;
        ImageView iv_up;
        ImageView iv_reply;
        TextView tv_up_count;

        ReplyHolder(View itemView) {
            super(itemView);
            iv_avatar = (RoundedImageView) itemView.findViewById(R.id.iv_avatar);
            tv_user_name = (TextView) itemView.findViewById(R.id.tv_user_name);
            tv_created_at = (TextView) itemView.findViewById(R.id.tv_created_at);
            tv_content = (RichTextView) itemView.findViewById(R.id.tv_content);
            iv_up = (ImageView) itemView.findViewById(R.id.iv_up);
            iv_reply = (ImageView) itemView.findViewById(R.id.iv_reply);
            tv_up_count = (TextView) itemView.findViewById(R.id.tv_up_count);
        }
    }

    private class ReplyCountHolder extends RecyclerView.ViewHolder {

        TextView tv_reply_count;

        ReplyCountHolder(View itemView) {
            super(itemView);
            tv_reply_count = (TextView) itemView.findViewById(R.id.tv_reply_count);
        }
    }

    private class EmptyHolder extends RecyclerView.ViewHolder {

        TextView tv_info;

        EmptyHolder(View itemView) {
            super(itemView);
            tv_info = (TextView) itemView.findViewById(R.id.tv_info);
            tv_info.setText(getString(R.string.empty_comments));
        }
    }
}
