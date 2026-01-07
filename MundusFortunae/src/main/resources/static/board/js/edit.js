/**
 * ✅ Summernote (WYSIWYG 에디터)
 * 
 * - HTML <textarea>를 '문서 편집기'처럼 바꿔주는 라이브러리
 * - bold, italic, underline, 리스트, 링크, 이미지 삽입 등 지원
 * - 작성 결과는 HTML 문자열 형태로 서버에 전송됨
 *   예: <p><b>굵은 글씨</b> 입니다.</p>
 */
function useSummernote(){
	$('#summernote').summernote({
	   placeholder: '내용을 입력하세요...', // 아무 내용 없을 때 표시되는 안내문
	   height: 300,                         // 에디터 높이(px)
	   
	   // 툴바 구성 (편집기 상단 버튼 세트), 이미지 추후 지원
	   toolbar: [
	     ['style', ['bold', 'italic', 'underline', 'clear']], // 굵게, 기울임, 밑줄, 서식 지우기
	     ['font', ['fontsize', 'color']],                     // 글자 크기, 색상
	     ['para', ['ul', 'ol', 'paragraph']],                 // 목록(ul/ol), 문단정렬
	/*      ['insert', ['link', 'picture']],                     // 링크, 이미지 삽입 */
	     ['insert', ['link']],                     // 링크
	     ['view', ['fullscreen', 'codeview']]                 // 전체화면, HTML코드보기
	   ],
	   
	   callbacks: {
		   onImageUpload : function(files){
			   //여러개 업로드 지원
			   for(let i = 0; i < files.length; i++){
				   uploadImage(files[i]);
			   }
		   }
	   }
	});
}

function textLengthCheck(){
	const textLengthEl = document.getElementById('text-length');
	
	//summernote는 jquery문법을 따라야함
	$("#summernote").on('summernote.change', () => {
		
		//HTML 포함된 코드
		const content = $('#summernote').summernote('code');
		
		//HTML 태그 제거 -> 순수 텍스트만 추출
		const plainText = $('<div>').html(content).text().trim();
		
		//글자수 계산
		const length = plainText.length;
		
		//글자수 표시
		textLengthEl.textContent = length + " / 5000";
		
		//색상변경
		if(length > 5000){
			setTimeout(() => {
				textLengthEl.style.color = 'red';
				//초과분 잘라내기 (순수 텍스트 기준으로)
				const trimmedText = plainText.substring(0, 5000);
				//HTML 태그 없이 순수 텍스트로 다시 세팅
				// 원본 포맷이 필요하다면 <p> 래핑 등 추가 가능
				$('#summernote').summernote('code', trimmedText);
				
				alert("최대 5000자까지만 입력하실 수 있습니다.");				
			}, 0);
		} else {
			textLengthEl.style.color = 'black';
		}
	});
}

//취소 안내(뒤로가기)
function cancelWrite(){
	const content = document.getElementById('summernote').value.trim();
	
	if(content.length > 0){
		const result = confirm("정말로 취소하시겠습니까?\n작성 중인 내용은 저장되지 않습니다.");
		if(result){
			history.back();
		}
	} else {
		history.back();
	}
}

//글쓰기 전송 요청
function sendForm(){
	const submitBtn = document.getElementById('edit-btn');
	if(!submitBtn){
		console.log("submitBtn 없음	");
		return;
	}
	
	submitBtn.addEventListener("click", async (e) => {
		e.preventDefault();
		const categorySeq = document.getElementById("category").value;
		const title = document.getElementById("title").value.trim();
		const content = $('#summernote').summernote('code');
		
		const imageFile = document.getElementById("imageFile")?.files?.[0] ?? null;
		const documentFile = document.getElementById("documentFile")?.files?.[0] ?? null;
		
		const boardSeq = document.getElementById('board-seq').value;
		
		const originImageFile = document.getElementById('origin-imageFile').value;
		
		console.log(categorySeq);
		
		if(!categorySeq ){
			alert("토픽을 선택해 주세요.");
			return;
		}
		
		if(!title || title === null){
			alert("제목을 이력해주세요");
			return;
		}
		
		/* 첨부파일이 있다면, FormData로 보내는게 정석 */
		const fd = new FormData;
		fd.append("categorySeq", categorySeq);
		fd.append("title", title);
		fd.append("content", content);
		if(imageFile){
			fd.append("imageFile", imageFile);
		}
		if(documentFile){
			fd.append("documentFile", documentFile);
		}
		fd.append("originImageFile", originImageFile);
		
		console.log("fetch URL =", contextPath + "board/edit/" + boardSeq);
		
		try {
			const response = await fetch (contextPath + "board/edit/" + boardSeq , {
				method:"POST",
				headers:{
					//"Content-Type":"application/json",
					"Authorization":"Bearer " + localStorage.getItem('accessToken')
				}, 
				body:fd
			});
			
			const result = await response.json();
			
			if(response.ok ){  
				//alert("게시물 작성 성공, result.data : " + result.data); // 3
				location.href=contextPath + "board/free";
				return;
			} else if(response.status === 401 ){ 		// 비로그인 => 권한없음 
				alert(result?.message || "로그인이 필요합니다. 다시 로그인해주세요.");
				location.href = contextPath + "login";
				return;
			} else if(response.status === 403){ 		//권한부족 (관리자만)
				alert(result?.message || "공지사항 작성에 접근 권한이 없습니다.");
				return;
			} else if(response.status === 413){			//413이 나면 스프링이 보통 JSON이 아니라 기본 에러 HTML을 내려줘
				alert("파일 용량이 너무 큽니다. 업로드 제한을 확인해주세요.");
				return;
			} else {
				//옵셔널 체이닝(optional chaining) -> null/undefined이면 → 거기서 멈추고 undefined를 반환(에러 안 남)
				const msg = result?.message || `요청 실패 (HTTP ${response.status})`; 
				alert(msg);
				
			}
		} catch(err) {
			console.error("통신 오류:", err);
			alert("서버와의 통신 중 오류가 발생했습니다.");
			return;
		}
	});
}

function uploadImageFile(){
	const file = document.getElementById("imageFile");
	file.addEventListener("change", (e) => {
		
		const input = e.target;
		const file = input.files?.[0];
		console.log(file);
		if(!file) return;
		
		//1차 체크 : MIME타입으로 	
		if(!file.type.startsWith('image/')){
			alert("이미지 파일만 가능합니다.");
			input.value = "";
			return;
		}
		
		//2차 체크 : 확장자 화이트리스트로
		const allowedExt = ["png", "jpg", "jpeg", "gif", "webp"];
		const ext = file.name.split(".").pop(); //마지막꺼 뽑아
		if(!allowedExt.includes(ext)){
			alert("이미지 파일만 가능합니다.");
			input.value = "";
			return;
		}
		
		//3차 체크 : 용량 확인 
		const maxSize = 5 * 1024 * 1024; //5Mb
		if(file.size > maxSize){
			alert("파일 용량은 5MB 이하만 가능합니다.");
			input.value = "";
			return;
		}
		
		console.log("OK image:", file.name, file.type, file.size);
	});
}
	
function uploadDocumentFile(){
	const file = document.getElementById("documentFile");
	if(!file){
		return;
	}
	file.addEventListener("change", (e) => {
		
		const input = e.target;
		const file = input.files?.[0];
		console.log(file);
		if(!file) return;
		
		//문서는 MIME검사 빡셈 통과 -
		
		//확장자 화이트 리스트 검사
		const allowedExt = ["pdf","doc","docx","xls","xlsx","ppt","pptx","txt","hwp"];
		const ext = file.name.split(".").pop().toLowerCase();
		if(!allowedExt.includes(ext)){
			alert("문서형식 파일만 첨부 가능합니다.");
			input.value = "";
			return;
		}
		
	  // (선택) 용량 제한 예: 10MB
	  const maxSize = 10 * 1024 * 1024;
	  if (file.size > maxSize) {
	    alert("파일 용량은 10MB 이하만 가능합니다.");
	    input.value = "";
	    return;
	  }
	  
	  console.log("OK document:", file.name, file.type, file.size);
	});
}

/**
 * ✅ Tagify (태그 입력 기능)
 * 
 * - 일반 input을 "태그 입력 필드"로 바꿔주는 JS 라이브러리
 * - 사용자는 엔터나 쉼표로 여러 개의 태그를 입력할 수 있음
 * - 입력된 태그들은 시각적으로 '말풍선' 형태로 표시됨
 * - 서버 전송 시엔 "쉼표로 구분된 문자열" 형태로 넘어감 (예: "자바,스프링,백엔드")
	 const tagInput = document.querySelector('#tagInput');
	 if (tagInput) {
	 	// Tagify 객체 생성
	    const tagify = new Tagify(tagInput, {
	    	maxTags: 5, // 최대 5개까지 입력 가능
	       
	 		// 드롭다운 옵션 (자동완성 기능 관련)
	     dropdown: {
	       enabled: 1,          // 1 이상이면 입력 시 자동완성창이 바로 나타남
	       maxItems: 5,         // 자동완성 항목 개수 제한
	       classname: "tags-look",  // 드롭다운창의 스타일 클래스 (기본값 유지 가능)
	       closeOnSelect: true  // 태그를 선택하면 드롭다운 자동으로 닫힘
	     }
	 	});
	 	// (선택사항) 입력된 태그 변경 이벤트 감지
	 	tagInput.addEventListener('change', (e) => {
	 	  console.log('현재 태그들:', e.target.value); // "자바,스프링,백엔드"
	 	});
	 }
*/
