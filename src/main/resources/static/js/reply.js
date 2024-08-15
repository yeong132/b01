async function getList({bno, page, size, goLast}){
    // bno : 게시물 번호
    // page : 요청할 페이지 번호
    // size : 한 페이지에 표시할 댓글 수
    // goLast : 마지막 페이지로 이동할지 여부를 결정하는 플래그

    // 주어진 게시물 번호(bno)에 해당하는 댓글 목록을 서버에 요청
    // 요청 URL /replies/list/${bno}
    // 쿼리 매개변수 page, size 전달
    const result = await axios.get(`/replies/list/${bno}`, {params: {page, size}});

    if(goLast){
        const total = result.data.total
        const lastPage = parseInt(Math.ceil(total/size))
        return getList({bno:bno, page:lastPage, size:size})
    }

    return result.data;
}

async function addReply(replyObj) {
    const response = await axios.post(`/replies/`, replyObj)
    return response.data
}

async function getReply(rno) {
    const response = await axios.get(`/replies/${rno}`)
    return response.data
}

async function modifyReply(replyObj) {
    const response = await axios.put(`/replies/${replyObj.rno}`, replyObj)
    return response.data
}

async function removeReply(rno) {
    const response = await axios.delete(`/replies/${rno}`)
    return response.data
}