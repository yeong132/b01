package org.zerock.b01.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.zerock.b01.domain.Board;
import org.zerock.b01.domain.Reply;
import org.zerock.b01.dto.PageRequestDTO;
import org.zerock.b01.dto.PageResponseDTO;
import org.zerock.b01.dto.ReplyDTO;
import org.zerock.b01.repository.ReplyRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReplyServiceImpl implements ReplyService {

    private final ReplyRepository replyRepository;
    private final ModelMapper modelMapper;

    // Reply 엔티티 객체가 Board 엔티티 객체를 참조하기 때문에 별도 처리 필요
    // ReplyDTO를 Reply 엔티티로 변환할 때 bno 값을 포함할 수 있도록 별도 처리하는 method 선언
    private Reply dtoToEntity(ReplyDTO replyDTO) {
        // Board 객체 생성하여 설정
        Board board = Board.builder().bno(replyDTO.getBno()).build();

        // Reply 객체를 생성하여 반환
        return Reply.builder()
                .board(board)
                .replyText(replyDTO.getReplyText())
                .replyer(replyDTO.getReplyer())
                .build();
    }

    // Reply 엔티티를 ReplyDTO로 변환할 때 bno 값을 포함할 수 있도록 별도 처리하는 method 선언
    private ReplyDTO entityToDto(Reply reply) {
        return ReplyDTO.builder()
                .rno(reply.getRno())
                .replyText(reply.getReplyText())
                .replyer(reply.getReplyer())
                .regDate(reply.getRegDate())
                .modDate(reply.getModDate())
                .bno(reply.getBoard().getBno())
                .build();
    }

    @Override
    public Long register(ReplyDTO replyDTO) {
        // Reply reply = modelMapper.map(replyDTO, Reply.class);
        Reply reply = dtoToEntity(replyDTO);
        return replyRepository.save(reply).getRno();
    }

    @Override
    public ReplyDTO read(Long rno) {
        Optional<Reply> replyOptional = replyRepository.findById(rno);
        Reply reply = replyOptional.orElseThrow();

        // return modelMapper.map(reply, ReplyDTO.class);

        return entityToDto(reply);
    }

    @Override
    public void modify(ReplyDTO replyDTO) {
        Optional<Reply> replyOptional = replyRepository.findById(replyDTO.getRno());
        Reply reply = replyOptional.orElseThrow();
        reply.changeText(replyDTO.getReplyText());; // 댓글의 내용만 수정 가능
        replyRepository.save(reply);
    }

    @Override
    public void remove(Long rno) {
        Optional<Reply> replyOptional = replyRepository.findById(rno);
        if(replyOptional.isEmpty()) {
            throw new NoSuchElementException(); // -- 해당 요소를 찾지 못했다는 예외 발생
        }

        replyRepository.deleteById(rno);
    }

    @Override
    public PageResponseDTO<ReplyDTO> getListOfBoard(Long bno, PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() <=0? 0:
                pageRequestDTO.getPage() -1,
                pageRequestDTO.getSize(),
                Sort.by("rno").ascending());

        Page<Reply> result = replyRepository.listOfBoard(bno, pageable);

        List<ReplyDTO> dtoList =
                result.getContent().stream().map(reply -> modelMapper.map(reply, ReplyDTO.class))
                        .collect(Collectors.toList());

        return PageResponseDTO.<ReplyDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }
}
