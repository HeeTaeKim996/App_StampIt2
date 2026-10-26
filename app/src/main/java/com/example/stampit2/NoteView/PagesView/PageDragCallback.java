package com.example.stampit2.NoteView.PagesView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class PageDragCallback extends ItemTouchHelper.SimpleCallback
{

    private final PagesAdapter adapter;

    private int dragStartPos = -1;
    private int dragEndPos = -1;

    public PageDragCallback(PagesAdapter inAdapter)
    {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0);
        // dragDirs : 드래그 방향 설정, swipeDirs : swipe(삭제, 메세지 보내기 등에 자주 사용되는 기능) 방향 설정 (0으로 했으니 미사용)

        adapter = inAdapter;
    }




    // 드래그 전에, 드래그가 가능한 상태인지 확인
    @Override
    public boolean isLongPressDragEnabled()
    {
        return adapter.isSelectMode() == false;
    }

    // 드래그 시작시 호출
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder vh, int actionState)
    {
        super.onSelectedChanged(vh, actionState);

        if(actionState != ItemTouchHelper.ACTION_STATE_DRAG || vh == null) return;

        dragStartPos = vh.getAdapterPosition();
        dragEndPos = dragStartPos;
    }


    // 드래그 도중 호출
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target)
    {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        if(from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;

        dragEndPos = to;

        adapter.notifyItemMoved(from, to);

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
    {
        // 좌우로 밀어서 아이템삭제/메세지보내기 등에 사용되는 함수 (사용안함)
    }

    // 드래그 종료시 호출
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
    {
        super.clearView(recyclerView, viewHolder);

        if(dragStartPos != -1 && dragStartPos != dragEndPos && adapter != null)
        {
            adapter.saveOnItemMoved(dragStartPos, dragEndPos);
        }

        dragStartPos = -1;
        dragEndPos = -1;
    }

}
