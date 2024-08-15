package org.zerock.b01.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.zerock.b01.domain.Board;
import org.zerock.b01.domain.QBoard;
import org.zerock.b01.domain.QReply;
import org.zerock.b01.dto.BoardListReplyCountDTO;

import java.util.List;

public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {
    public BoardSearchImpl() {
        super(Board.class);
    }

    // Pageable 객체를 받아 페이징된 결과를 반환하는 메서드
    @Override
    public Page<Board> search1(Pageable pageable) {

        // QBoard 객체는 QueryDSL이 `Board` 엔티티를 기반으로 생성한 Q 클래스 -- 타입 세이프한 쿼리 작성
        QBoard board = QBoard.board; // Q도메인 객체

        // from(board) => Board 엔티티를 대상으로 하는 JPQL 쿼리를 생성 (SELECT ... FROM board와 유사)
        JPQLQuery<Board> query = from(board); // select... from board

        // BooleanBuilder를 사용하여 여러 조건을 조합 (OR, AND 등) : '()'로 묶는다
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // [OR 조건 추가]
        // title 필드가 11을 포함하는 조건 추가
        booleanBuilder.or(board.title.contains("11"));

        // content 필드가 11을 포함하는 조건 추가
        booleanBuilder.or(board.content.contains("11"));

        query.where(booleanBuilder); // -- 최종적으로 booleanBuilder를 where 절에 추가

        // bno > 0 -- bno 필드가 0보다 큰 레코드를 조회하는 기본 조건 추가 -- 앞선 조건과 AND로 결합
        query.where(board.bno.gt(0L));

/*        // where(board.title.contains("1") => title 필드가 1을 포함하는 레코드를 조회하는 건을 추가 (WHERE title LIKE '%1%')
        query.where(board.title.contains("1")); // where title like...*/

        // paging -- applyPagination()를 활용하여 pageable 객체를 쿼리에 적용 (pageable : 페이지 번호와 페이지 크기 등 페이징 정보가 반영)
        this.getQuerydsl().applyPagination(pageable, query);

        // 쿼리 실행 및 결과 조회
        // fetch() -- 쿼리 실행
        List<Board> list = query.fetch();

        // fetchCount() -- 쿼리를 실행하여 조건에 맞는 전체 레코드 수를 반환
        long count = query.fetchCount();

        return null;
    }

    @Override
    public Page<Board> searchAll(String[] types, String keyword, Pageable pageable) {
        QBoard board = QBoard.board;
        JPQLQuery<Board> query = from(board);

        if((types != null && (types.length > 0) && keyword != null)){ //검색 조건과 키워드가 있다면
            BooleanBuilder booleanBuilder = new BooleanBuilder(); // (
                for (String type : types) {
                    switch (type) {
                        case "t":
                            booleanBuilder.or(board.title.contains(keyword));
                            break;
                        case "c":
                            booleanBuilder.or(board.content.contains(keyword));
                            break;
                        case "w":
                            booleanBuilder.or(board.writer.contains(keyword));
                            break;
                    }
                }//end for
                query.where(booleanBuilder);
            }//end if

            //bno > 0
            query.where(board.bno.gt(0L));

            //paging
            this.getQuerydsl().applyPagination(pageable, query);
            List<Board> list = query.fetch();
            long count = query.fetchCount();
            return new PageImpl<>(list, pageable, count);
    }

    @Override
    public Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types, String keyword, Pageable pageable) {
        QBoard board = QBoard.board;
        QReply reply = QReply.reply;

        JPQLQuery<Board> query = from(board);
        query.leftJoin(reply).on(reply.board.eq(board));

        query.groupBy(board);

        if((types != null && (types.length > 0) && keyword != null)){ //검색 조건과 키워드가 있다면
            BooleanBuilder booleanBuilder = new BooleanBuilder(); // (
            for (String type : types) {
                switch (type) {
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }//end for
            query.where(booleanBuilder);
        }//end if

        //bno > 0
        query.where(board.bno.gt(0L));

        JPQLQuery<BoardListReplyCountDTO> dtoQuery = query.select(Projections.
                bean(BoardListReplyCountDTO.class,
                        board.bno,
                        board.title,
                        board.writer,
                        board.regDate,
                        reply.count().as("replyCount")
                ));

        this.getQuerydsl().applyPagination(pageable, dtoQuery);
        List<BoardListReplyCountDTO> dtoList = dtoQuery.fetch();
        long count = dtoQuery.fetchCount();
        return new PageImpl<>(dtoList, pageable, count);
    }
}
