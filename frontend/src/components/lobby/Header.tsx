import React, { useEffect, useState } from 'react';

import { ImExit } from 'react-icons/im';
import { BsFillTrophyFill } from 'react-icons/bs';
import { IoMdRefresh } from 'react-icons/io';
import { FaForward } from 'react-icons/fa6';
import { MdAddHome } from 'react-icons/md';
import { Link } from 'react-router-dom';
import CreateRoom from './CreateRoom';
import { LobbyApi } from '../../hooks/axios-lobby';
// interface Props {
//   channelId: number;
// }
interface Props {
  channelId: number;
  handleState: (data: RoomProps[]) => void;
}
// const Header = ({ channelId }: Props, handleState: (data: RoomProps[]) => void) => {
const Header = ({ channelId, handleState }: Props) => {
  const fastMatching = () => {
    alert('빠른대전 매칭완료!');
  };
  const refresh = async () => {
    alert('새로고침 버튼 누름');

    const { data } = await LobbyApi.getGameList(channelId);
    console.log('이거는 패치할 데이터 정보임');

    console.log(data);
    handleState(data);
  };
  const ranking = () => {};

  return (
    <nav className="flex flex-col w-full">
      <div className="w-full h-[10vh] bg-red-200">자유 {channelId} 채널</div>
      <div className="w-full h-[10vh] flex">
        <CreateRoom channelId={channelId} />
        <button className="w-1/4 h-[10vh] btn-mint" onClick={fastMatching}>
          <FaForward />
          빠른대전
        </button>
        <button className="w-1/4 h-[10vh] btn-mint" onClick={refresh}>
          <IoMdRefresh />
          새로고침
        </button>
        <button className="w-1/4 h-[10vh] btn-mint">
          <BsFillTrophyFill />
          랭킹
        </button>
      </div>

      <div>
        <Link to="/channel">
          <button className="w-1/4 h-[10vh] bg-red-500">
            <ImExit />
            나가기
          </button>
        </Link>
      </div>
    </nav>
  );
};

export default Header;
