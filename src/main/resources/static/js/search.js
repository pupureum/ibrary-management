$(function() {
    $('#btn-search-api').on('click', function (event) {
        event.preventDefault();
        let keyword = $('#keyword').val();

        $.ajax({
            type: 'GET',
            url: 'api/book',
            data: {keyword: keyword},
            dataType:  'json',
            success: function (response) {
                // 요청 성공 시 도서 정보를 받아옴
                $('#bookModal').modal('show');

                // 모달 창 내부에 도서 정보 추가
                var modalBody = $('#bookModal').find('.modal-body');
                modalBody.empty(); // 기존 도서 정보 제거

                response.forEach(function(book) {
                    var bookItem = $('<div>').addClass('book-item');
                    $('<h5>').addClass('book-title').text(book.title).appendTo(bookItem);
                    var selectedBookImage = book.image;
                    var imageElement = $('<img>').addClass('book-image').attr('src', selectedBookImage);
                    imageElement.appendTo(bookItem);
                    $('<p>').addClass('book-isbn').text('ISBN: ' + book.isbn).appendTo(bookItem);
                    $('<p>').addClass('book-author').text('저자: ' + book.author).appendTo(bookItem);
                    $('<p>').addClass('book-publisher').text('출판사: ' + book.publisher).appendTo(bookItem);
                    // $('<p>').addClass('book-description').text('설명: ' + book.description).appendTo(bookItem);
                    $('<p>').addClass('book-pubDate').text('출판일: ' + book.pubDate).appendTo(bookItem);
                    var selectButton = $('<button>').addClass('btn btn-outline-primary btn-select-book').text('선택');

                    selectButton.click(function() {
                        // 선택 버튼이 클릭되었을 때 실행되는 동작
                        // 선택된 도서 정보 추출
                        var selectedBookIsbn = book.isbn;
                        var selectedBookTitle = book.title;
                        var selectedBookAuthor = book.author;
                        var selectedBookPublisher = book.publisher;
                        var selectedBookDescription = book.description;
                        var selectedBookImage = book.image;
                        var selectedBookPubDate = book.pubDate;

                        // 추출된 도서 정보를 사용하여 필요한 동작 수행
                        $('#selectedBookIsbn').val(selectedBookIsbn);
                        $('#selectedBookTitle').val(selectedBookTitle);
                        $('#selectedBookAuthor').val(selectedBookAuthor);
                        $('#selectedBookPublisher').val(selectedBookPublisher);
                        $('#selectedBookDescription').val(selectedBookDescription);
                        $('#selectedBookImage').val(selectedBookImage);
                        $('#selectedBookPubDate').val(selectedBookPubDate);

                        // 폼을 보이도록 설정
                        $('#bookModal').modal('hide');
                        $('#selectedBookForm').show();

                    });
                    bookItem.append(selectButton);
                    $('.modal-body').append(bookItem);
                });
            },
            error: function (error) {
                console.log('search 오류 발생:', error);
            }
        });
    });
});