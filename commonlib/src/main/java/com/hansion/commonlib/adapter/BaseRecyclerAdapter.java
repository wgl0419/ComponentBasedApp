package com.hansion.commonlib.adapter;

import android.animation.Animator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 陈锦军 on 16/3/12.
 */
public abstract class BaseRecyclerAdapter<T, K extends BaseWrappedViewHolder> extends RecyclerView.Adapter<K> {


    protected static final int REFRESH_HEADER = Integer.MIN_VALUE;
    protected static final int HEADER = Integer.MIN_VALUE + 1;
    protected static final int FOOTER = Integer.MAX_VALUE - 1;
    protected static final int LOAD_MORE_FOOTER = Integer.MAX_VALUE;
    protected static final int EMPTY = Integer.MIN_VALUE + 2;


    private RefreshHeaderLayout mRefreshHeaderContainer;

    private FrameLayout mLoadMoreFooterContainer;

    private LinearLayout mHeaderContainer;

    private LinearLayout mFooterContainer;


    protected List<T> data;

    private int layoutId;
    private int lastLayoutPosition = -1;

    public BaseRecyclerAdapter(List<T> data, int layoutId) {
        this.data = data == null ? new ArrayList<T>() : data;
        if (layoutId != 0)
            this.layoutId = layoutId;
        if (getLayoutId()!=0) {
            this.layoutId = getLayoutId();
        }
    }

    protected abstract int getLayoutId();


    public BaseRecyclerAdapter(List<T> data) {
        this(data, 0);
    }

    public BaseRecyclerAdapter(int layoutId) {
        this(null, layoutId);
    }

    public BaseRecyclerAdapter() {
        this(null);
    }

    private LayoutInflater mLayoutInflater;

    public void setHeaderContainer(LinearLayout mHeaderContainer) {
        this.mHeaderContainer = mHeaderContainer;
    }


    public void setFooterContainer(LinearLayout mFooterContainer) {
        this.mFooterContainer = mFooterContainer;
    }


    public void setRefreshHeaderContainer(RefreshHeaderLayout mRefreshHeaderContainer) {
        this.mRefreshHeaderContainer = mRefreshHeaderContainer;
    }


    public void setLoadMoreFooterContainer(FrameLayout mLoadMoreFooterContainer) {
        this.mLoadMoreFooterContainer = mLoadMoreFooterContainer;
    }


    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    BaseRecyclerAdapter wrapperAdapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
                    if (isFullSpanType(wrapperAdapter.getItemViewType(position))) {
                        return gridLayoutManager.getSpanCount();
                    } else if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position - getItemUpCount());
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        int type = getItemViewType(position);
        if (isFullSpanType(type)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }




    private int marginSize=0;


    public int getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(int marginSize) {
        this.marginSize = marginSize;
    }

    private boolean isFullSpanType(int type) {
        return type == REFRESH_HEADER || type == HEADER || type == FOOTER || type == LOAD_MORE_FOOTER || type == EMPTY;
    }

    @Override
    public int getItemCount() {
        return data.size() + getItemUpCount() + getItemDownCount();
    }

    private int getItemDownCount() {
        int position = 0;
        if (hasFootView()) {
            position++;
        }
        if (hasMoreLoadView()) {
            position++;
        }
        return position;
    }


    public boolean hasRefreshableView() {
        return mRefreshHeaderContainer != null && mRefreshHeaderContainer.getChildCount() > 0;
    }


    private boolean hasHeaderView() {
        return mHeaderContainer != null && mHeaderContainer.getChildCount() > 0;
    }


    private boolean hasFootView() {
        return mFooterContainer != null && mFooterContainer.getChildCount() > 0;
    }


    private boolean hasMoreLoadView() {
        return mLoadMoreFooterContainer != null && mLoadMoreFooterContainer.getChildCount() > 0;
    }


    public int getItemUpCount() {
        int position = 0;
        if (hasHeaderView()) {
            position++;
        }
        if (hasRefreshableView()) {
            position++;
        }
        return position;
    }


    @Override
    public int getItemViewType(int position) {
        position = getRealPosition(position);
        int size=data.size();
        if (position == 0) {
            return REFRESH_HEADER;
        } else if (position == 1) {
            return HEADER;
        } else if (1 < position && position < size + 2) {
            return getDefaultItemViewType(position - 2);
        } else if (position == size + 2) {
            if (hasFootView()) {
                return FOOTER;
            }else{
                return LOAD_MORE_FOOTER;
            }
        } else if (position == size + 3) {
            return LOAD_MORE_FOOTER;
        }else {
            return -1;
        }
    }


    protected int getDefaultItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        int realPosition = getRealPosition(position);
        if (1 < realPosition && realPosition < data.size() + 2) {
            Animator[] itemAnimator=getItemAnimator(holder);
            if (itemAnimator!=null) {
                for (Animator anim :itemAnimator) {
                    anim.setInterpolator(interpolator);
                    anim.setDuration(duration).start();
                }
            }
            convert(holder, data.get(realPosition - 2),position);
        }
    }





    protected Animator[] getItemAnimator(BaseWrappedViewHolder holder){
        return null;
    };


    private int duration=300;
    private LinearInterpolator interpolator=new LinearInterpolator();


    private int getRealPosition(int position) {
        int realPosition = position;
        if (!hasRefreshableView() && !hasHeaderView()) {
            realPosition = realPosition + 2;
        } else if (!hasRefreshableView()) {
            realPosition++;
        } else if (!hasHeaderView()) {
            if (position >= 1) {
                realPosition++;
            }
        }
        return realPosition;
    }

    protected abstract void convert(K holder, T data,int position);

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        K k = createBaseViewHolder(getLayoutFromViewType(parent, viewType));
        return k;
    }


    private View getLayoutFromViewType(ViewGroup parent, int viewType) {
        switch (viewType) {
            case REFRESH_HEADER:
                return mRefreshHeaderContainer;
            case HEADER:
                return mHeaderContainer;
            case FOOTER:
                return mFooterContainer;
            case LOAD_MORE_FOOTER:
                return mLoadMoreFooterContainer;
            default:
                return mLayoutInflater.inflate(layoutId, parent, false);
        }
    }

    K createBaseViewHolder(View view) {
        Class adapterClass = getClass();
        Class resultClass = null;
        while (resultClass == null && adapterClass != null) {
            resultClass = getInstancedGenericKClass(adapterClass);
            adapterClass = adapterClass.getSuperclass();
        }
        K k = createGenericKInstance(resultClass, view);
        if (k != null) {
            k.bindAdapter(this);
        }
        return k != null ? k : (K) new BaseWrappedViewHolder(view);
    }

    private K createGenericKInstance(Class aClass, View view) {
        try {
//                构造函数
            Constructor constructor;
//                获取修饰符
            String modifier = Modifier.toString(aClass.getModifiers());
            String name = aClass.getName();

//                内部类并且不是静态类
            if (name.contains("$") && !modifier.contains("static")) {
                constructor = aClass.getDeclaredConstructor(getClass(), View.class);
                return (K) constructor.newInstance(this, view);
            }
            constructor = aClass.getDeclaredConstructor(View.class);
            return (K) constructor.newInstance(view);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class getInstancedGenericKClass(Class adapterClass) {
        Type type = adapterClass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type type1 :
                    types) {
                if (type1 instanceof Class) {
                    Class class1 = (Class) type1;
                    if (BaseWrappedViewHolder.class.isAssignableFrom(class1)) {
                        return class1;
                    }
                }
            }
        }
        return null;
    }


    public void addData(int position, List<T> newData) {
        if (newData == null || newData.size() == 0) {
            notifyLoadMoreChanged();
            return;
        }
        List<T> temp = new ArrayList<>();
        List<T> copyData = new ArrayList<>(data);
        for (T data :
                newData) {
            for (T item :
                    copyData) {
                if (data.equals(item)) {
                    temp.add(data);
                }
            }
        }
        if (temp.size() > 0) {
            int index;
            for (T item :
                    temp) {
                index = data.indexOf(item);
                data.set(index, item);
                newData.remove(item);
            }
            notifyDataSetChanged();
            addData(position, newData);
        } else {
            data.addAll(position, newData);
            notifyItemRangeInserted(position + getItemUpCount(), newData.size());
            notifyLoadMoreChanged();
        }
    }


    private void notifyLoadMoreChanged() {
        if (hasMoreLoadView()) {
            int lastVisiblePosition;
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
            } else{
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                lastVisiblePosition = gridLayoutManager.findLastVisibleItemPositions(null)[0];
            }
            int position;
            position = getItemUpCount() + getData().size()+1;
            if (lastVisiblePosition!=-1&&lastVisiblePosition == position) {
                if (mLoadMoreFooterContainer.getChildAt(0) instanceof LoadMoreFooterView) {
                    ((LoadMoreFooterView) mLoadMoreFooterContainer.getChildAt(0)).setStatus(LoadMoreFooterView.Status.THE_END);
                }
            }
        }
    }


    public void addData(T newData) {
        addData(data.size(), newData);
    }


    public void addData(int position, T newData) {
        if (newData != null) {
            if (!data.contains(newData)) {
                data.add(position, newData);
                notifyItemInserted(position + getItemUpCount());
            } else {
                int index = data.indexOf(newData);
                data.set(index, newData);
            }
        }
    }

    public void addData(List<T> newData) {
        addData(data.size(), newData);
    }

    public List<T> getData() {
        return data;
    }

    public void clearAllData() {
        data.clear();
    }

    public void clear(){
        data.clear();
        notifyDataSetChanged();
    }

    public T getData(int position) {
        if (position < 0 || position > data.size() - 1) {
            return null;
        } else {
            return data.get(position);
        }
    }

    public T removeData(int position) {
        if (position >= 0 && position < data.size()) {
            T t = data.remove(position);
            notifyItemRemoved(position);
            return t;
        }
        return null;
    }


    private RecyclerView.LayoutManager layoutManager;

    public void bindManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;

    }

    public void refreshData(List<T> list) {
        clearAllData();
        notifyDataSetChanged();
        addData(list);
        layoutManager.scrollToPosition(0);
    }

    public interface OnItemClickListener {
         void onItemClick(int position, View view);

         boolean onItemLongClick(int position, View view);

         boolean onItemChildLongClick(int position, View view, int id);

         void onItemChildClick(int position, View view, int id);
    }


    private OnItemClickListener onItemClickListener;


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }
}
