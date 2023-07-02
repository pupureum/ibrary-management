$(function () {
    $('#btn-search-api').on('click', function (event) {
        event.preventDefault();
        let keyword = $('#keyword').val();
        if (keyword.trim() === "") {
            alert("키워드를 입력해주세요.");
            return;
        }

        $.ajax({
            type: 'GET',
            url: '/books/api/book',
            data: {keyword: keyword},
            dataType: 'json',
            success: function (response) {
                $('#bookModal').modal('show');
                let modalBody = $('#bookModal').find('.modal-body');
                modalBody.empty();

                $('<p>').addClass('total-books').text(response.total + '개의 검색 결과가 있습니다!').css('color', 'blue').appendTo(modalBody);
                response.items.forEach(function (book) {
                    let bookItem = $('<div>').addClass('book-item');
                    $('<hr>').appendTo(bookItem);
                    $('<h6>').addClass('book-title').text(book.title).appendTo(bookItem);
                    let selectedBookImage = book.image;
                    let imageElement = $('<img>').addClass('book-image').attr('src', selectedBookImage);
                    imageElement.appendTo(bookItem);
                    $('<p>').addClass('book-isbn').text('ISBN: ' + book.isbn).appendTo(bookItem);
                    $('<p>').addClass('book-author').text('저자: ' + book.author).appendTo(bookItem);
                    $('<p>').addClass('book-publisher').text('출판사: ' + book.publisher).appendTo(bookItem);
                    $('<p>').addClass('book-pubDate').text('출판일: ' + book.pubdate).appendTo(bookItem);
                    let selectButton = $('<button>').addClass('btn btn-outline-primary btn-select-book').text('선택');

                    selectButton.click(function () {
                        // 선택된 도서 정보 추출
                        let selectedBookIsbn = book.isbn;
                        let selectedBookTitle = book.title;
                        let selectedBookAuthor = book.author;
                        let selectedBookPublisher = book.publisher;
                        let selectedBookDescription = book.description;
                        let selectedBookImage = book.image;
                        let selectedBookPubDate = book.pubdate;

                        $('#selectedBookIsbn').val(selectedBookIsbn);
                        $('#selectedBookTitle').val(selectedBookTitle);
                        $('#selectedBookAuthor').val(selectedBookAuthor);
                        $('#selectedBookPublisher').val(selectedBookPublisher);
                        $('#selectedBookDescription').val(selectedBookDescription);
                        $('#selectedBookImage').val(selectedBookImage);
                        $('#selectedBookPubDate').val(selectedBookPubDate);

                        $('#bookModal').modal('hide');
                        // 폼이 보이도록 설정
                        $('#selectedBookForm').show();

                    });
                    bookItem.append(selectButton);
                    $('<hr>').appendTo(bookItem);
                    $('.modal-body').append(bookItem);
                });
            },
            error: function (error) {
                console.log('search 오류 발생:', error);
            }
        });
    });
});