async function uploadToServer (formObj) {

    console.log("upload to server......")
    console.log(formObj)

    /* axios 이용하여 POST 요청 */
    const response = await axios({
        method: 'post',
        url: '/upload',
        data: formObj,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });

    return response.data;   // 서버로부터 받은 응답 데이터 반환
}

/* 서버에서 파일을 삭제하는 비동기 함수 선언 */
async function removeFileToServer({uuid, fileName}) {

    const response = await axios.delete(`/remove/${uuid}_${fileName}`)

    return response.data
}